import { type ArgumentsHost, Catch, type ExceptionFilter } from "@nestjs/common";
import type { NextFunction, Request, Response } from "express";
import { error as validatorErrors } from "express-openapi-validator";
import type { components } from "../../../../generated/openapi/cook-assistance.js";
import { RecipeCatalogUnavailable } from "../../../application/ports/recipe-catalog.js";
import { TopicNotYetSupported } from "../../../application/topic-not-yet-supported.js";
import { ConcurrencyConflict } from "../../../domain/shared/concurrency-conflict.js";
import { DomainRejection } from "../../../domain/shared/domain-rejection.js";
import { Unauthenticated } from "./authentication.js";
import { NotAuthorized } from "./authorization.js";

export type Problem = components["schemas"]["Problem"];

const PROBLEM_TYPE_BASE = "https://larder.org/problems/cook-assistance/";

/** `StepNotInRecipe` → `step-not-in-recipe` (ADR-CA-006). */
function kebab(code: string): string {
  return code.replace(/(?<!^)([A-Z])/g, "-$1").toLowerCase();
}

function problemOf(status: number, code: string, detail?: string): Problem {
  return { type: `${PROBLEM_TYPE_BASE}${kebab(code)}`, title: code, status, code, ...(detail === undefined ? {} : { detail }) };
}

// ADR-CA-006 · the one error map: every error the HTTP adapter can meet → a Problem.
export function toProblem(error: unknown): Problem {
  if (error instanceof validatorErrors.BadRequest) return problemOf(400, "InvalidRequest", error.message);
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

function send(response: Response, problem: Problem): void {
  if (problem.status === 503) {
    response.setHeader("Retry-After", String(RETRY_AFTER_SECONDS));
  }
  response.status(problem.status).type("application/problem+json").json(problem);
}

/** Errors thrown in controllers (Nest). */
@Catch()
export class ProblemFilter implements ExceptionFilter {
  catch(error: unknown, host: ArgumentsHost): void {
    send(host.switchToHttp().getResponse<Response>(), toProblem(error));
  }
}

/** Errors passed on by Express middleware — the spec validator — which never reach a Nest filter. */
// eslint-disable-next-line max-params -- Express recognizes error handlers by their four parameters
export function problemErrorHandler(error: unknown, _request: Request, response: Response, _next: NextFunction): void {
  send(response, toProblem(error));
}
