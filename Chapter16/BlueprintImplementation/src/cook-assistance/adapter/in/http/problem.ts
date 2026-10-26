import { type ArgumentsHost, Catch, type ExceptionFilter, HttpException } from "@nestjs/common";
import type { NextFunction, Request, Response } from "express";
import { error as validatorErrors } from "express-openapi-validator";
import type { components } from "../../../../generated/openapi/cook-assistance.js";
import { RecipeCatalogUnavailable } from "../../../application/ports/recipe-catalog.js";
import { HelpNotFound } from "../../../application/queries/help-queries.js";
import { HelpRequestNotFound } from "../../../application/queries/help-request-queries.js";
import { TopicNotYetSupported } from "../../../application/topic-not-yet-supported.js";
import { ConcurrencyConflict } from "../../../domain/shared/concurrency-conflict.js";
import { DomainRejection } from "../../../domain/shared/domain-rejection.js";
import { Unauthenticated } from "./authentication.js";
import { NotAuthorized } from "./authorization.js";
import { MAX_BODY_MEGABYTES, TooManyRequests } from "./edge-limits.js";
import type { HttpLogEvent } from "./http-settings.js";

export type Problem = components["schemas"]["Problem"];

const PROBLEM_TYPE_BASE = "https://larder.org/problems/cook-assistance/";

/** `StepNotInRecipe` → `step-not-in-recipe` (ADR-CA-006). */
function kebab(code: string): string {
  return code.replace(/(?<!^)([A-Z])/g, "-$1").toLowerCase();
}

function problemOf(status: number, code: string, detail: string): Problem {
  return { type: `${PROBLEM_TYPE_BASE}${kebab(code)}`, title: code, status, code, detail };
}

/** body-parser (express.json) marks its own errors with a documented `type`, e.g. `entity.too.large`. */
function bodyParserErrorTypeOf(error: unknown): string | undefined {
  return typeof error === "object" && error !== null && "type" in error ? String(error.type) : undefined;
}

// ADR-CA-006 · the one error map: every error the HTTP adapter can meet → a Problem.
export function toProblem(error: unknown): Problem {
  if (error instanceof validatorErrors.BadRequest) return problemOf(400, "InvalidRequest", error.message);
  // Nest wraps a body that is not valid JSON in its own BadRequestException before any filter sees it
  if (error instanceof HttpException && error.getStatus() === 400) return problemOf(400, "InvalidRequest", "the body is not valid JSON");
  if (bodyParserErrorTypeOf(error) === "entity.too.large")
    return problemOf(413, "PayloadTooLarge", `the body is larger than ${MAX_BODY_MEGABYTES} MB`);
  if (error instanceof TooManyRequests) return problemOf(429, "TooManyRequests", error.message);
  if (error instanceof validatorErrors.NotFound) return problemOf(404, "NotFound", error.message);
  if (error instanceof HelpRequestNotFound) return problemOf(404, "NotFound", error.message);
  if (error instanceof HelpNotFound) return problemOf(404, "NotFound", error.message);
  if (error instanceof validatorErrors.MethodNotAllowed) return problemOf(405, "MethodNotAllowed", error.message);
  if (error instanceof validatorErrors.UnsupportedMediaType) return problemOf(415, "UnsupportedMediaType", error.message);
  if (error instanceof Unauthenticated) return problemOf(401, "Unauthenticated", error.message);
  if (error instanceof NotAuthorized) return { ...problemOf(403, "NotAuthorized", error.message), rule: null };
  if (error instanceof DomainRejection) return { ...problemOf(422, error.name, error.message), rule: error.rule };
  if (error instanceof TopicNotYetSupported) return { ...problemOf(422, error.name, error.message), rule: null };
  if (error instanceof ConcurrencyConflict) return { ...problemOf(409, "ConcurrencyConflict", error.message), rule: null };
  if (error instanceof RecipeCatalogUnavailable) return problemOf(503, "ServiceUnavailable", error.message);
  return problemOf(500, "InternalError", "an unexpected error occurred");
}

/** ADR-CA-015 · the Recipe Catalog circuit breaker tries again after 30 s — no sooner is worth a retry. */
const RETRY_AFTER_SECONDS = 30;

type Log = (event: HttpLogEvent) => void;

/** The request's path without its query: what the Problem's `instance` names (ADR-CA-006, inspection m-7). */
function pathOf(request: Request): string {
  return request.originalUrl.replace(/\?.*$/, "");
}

/**
 * ADR-CA-010 · refused accesses are security events, unexpected errors are reported — structured, without personal
 * data: no token, no cook id, no request body (QS-19, AP0007; inspection M-3, m-6).
 */
function report(log: Log, { request, problem, error }: { request: Request; problem: Problem; error: unknown }): void {
  const where = { method: request.method, path: pathOf(request) };
  if (problem.status === 401 || problem.status === 403) {
    log({ event: "access-refused", status: problem.status, code: problem.code, ...where, reason: (error as Error).message });
  } else if (problem.status === 500) {
    log({ event: "internal-error", ...where, error: error instanceof Error ? error.name : typeof error });
  }
}

// eslint-disable-next-line max-params -- the request, its answer, what went wrong and where it is reported
function send(request: Request, response: Response, error: unknown, log: Log): void {
  const problem = { ...toProblem(error), instance: pathOf(request) };
  report(log, { request, problem, error });
  if (problem.status === 503) {
    response.setHeader("Retry-After", String(RETRY_AFTER_SECONDS));
  }
  if (error instanceof TooManyRequests) {
    response.setHeader("Retry-After", String(error.retryAfterSeconds));
  }
  // RFC 9110 · a 405 names the methods the spec declares for the path
  if (error instanceof validatorErrors.MethodNotAllowed) {
    response.setHeader("Allow", String(error.headers?.Allow ?? "").toUpperCase());
  }
  response.status(problem.status).type("application/problem+json").json(problem);
}

/** Errors thrown in controllers (Nest). */
@Catch()
export class ProblemFilter implements ExceptionFilter {
  constructor(private readonly log: Log) {}

  catch(error: unknown, host: ArgumentsHost): void {
    const http = host.switchToHttp();
    send(http.getRequest<Request>(), http.getResponse<Response>(), error, this.log);
  }
}

/** Errors passed on by Express middleware — identification, the rate limit, the spec validator — which never reach a Nest filter. */
export function problemErrorHandlerFor(log: Log) {
  // eslint-disable-next-line max-params -- Express recognizes error handlers by their four parameters
  return (error: unknown, request: Request, response: Response, _next: NextFunction): void => send(request, response, error, log);
}
