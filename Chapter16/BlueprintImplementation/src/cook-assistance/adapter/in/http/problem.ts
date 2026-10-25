import { type ArgumentsHost, Catch, type ExceptionFilter, HttpException } from "@nestjs/common";
import type { NextFunction, Request, Response } from "express";
import { error as validatorErrors } from "express-openapi-validator";
import type { components } from "../../../../generated/openapi/cook-assistance.js";
import { RecipeCatalogUnavailable } from "../../../application/ports/recipe-catalog.js";
import { TopicNotYetSupported } from "../../../application/topic-not-yet-supported.js";
import { ConcurrencyConflict } from "../../../domain/shared/concurrency-conflict.js";
import { DomainRejection } from "../../../domain/shared/domain-rejection.js";
import { Unauthenticated } from "./authentication.js";
import { NotAuthorized } from "./authorization.js";
import { MAX_BODY_MEGABYTES, REQUESTS_PER_MINUTE } from "./edge-limits.js";

export type Problem = components["schemas"]["Problem"];

const PROBLEM_TYPE_BASE = "https://larder.org/problems/cook-assistance/";

/** `StepNotInRecipe` → `step-not-in-recipe` (ADR-CA-006). */
function kebab(code: string): string {
  return code.replace(/(?<!^)([A-Z])/g, "-$1").toLowerCase();
}

function problemOf(status: number, code: string, detail?: string): Problem {
  return { type: `${PROBLEM_TYPE_BASE}${kebab(code)}`, title: code, status, code, ...(detail === undefined ? {} : { detail }) };
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
  if (bodyParserErrorTypeOf(error) === "entity.too.large") return problemOf(413, "PayloadTooLarge", `the body is larger than ${MAX_BODY_MEGABYTES} MB`);
  if (error instanceof HttpException && error.getStatus() === 429) return problemOf(429, "TooManyRequests", `more than ${REQUESTS_PER_MINUTE} requests per minute`);
  if (error instanceof validatorErrors.NotFound) return problemOf(404, "NotFound", error.message);
  if (error instanceof validatorErrors.MethodNotAllowed) return problemOf(405, "MethodNotAllowed", error.message);
  if (error instanceof validatorErrors.UnsupportedMediaType) return problemOf(415, "UnsupportedMediaType", error.message);
  if (error instanceof Unauthenticated) return problemOf(401, "Unauthenticated", error.message);
  if (error instanceof NotAuthorized) return { ...problemOf(403, "NotAuthorized", error.message), rule: null };
  if (error instanceof DomainRejection) return { ...problemOf(422, error.name, error.message), rule: error.rule };
  if (error instanceof TopicNotYetSupported) return { ...problemOf(422, error.name, error.message), rule: null };
  if (error instanceof ConcurrencyConflict) return { ...problemOf(409, "ConcurrencyConflict", error.message), rule: null };
  if (error instanceof RecipeCatalogUnavailable) return problemOf(503, "ServiceUnavailable", error.message);
  return problemOf(500, "InternalError");
}

/** ADR-CA-015 · the Recipe Catalog circuit breaker tries again after 30 s — no sooner is worth a retry. */
const RETRY_AFTER_SECONDS = 30;

function send(response: Response, problem: Problem, error: unknown): void {
  if (problem.status === 503) {
    response.setHeader("Retry-After", String(RETRY_AFTER_SECONDS));
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
  catch(error: unknown, host: ArgumentsHost): void {
    send(host.switchToHttp().getResponse<Response>(), toProblem(error), error);
  }
}

/** Errors passed on by Express middleware — the spec validator — which never reach a Nest filter. */
// eslint-disable-next-line max-params -- Express recognizes error handlers by their four parameters
export function problemErrorHandler(error: unknown, _request: Request, response: Response, _next: NextFunction): void {
  send(response, toProblem(error), error);
}
