import { createParamDecorator, type ExecutionContext } from "@nestjs/common";
import type { NextFunction, Request, RequestHandler, Response } from "express";
import type { Actor } from "../../../application/actor.js";
import type { JoseTokenVerifier } from "./token-verifier.js";

/** No valid bearer token (ADR-CA-008): answered with 401, before any use case runs. */
export class Unauthenticated extends Error {
  constructor(reason: string) {
    super(reason);
    this.name = "Unauthenticated";
  }
}

const BEARER = "Bearer ";

/** The token of an `Authorization: Bearer <token>` header. */
export function bearerTokenOf(authorization: string | undefined): string {
  if (authorization === undefined || !authorization.startsWith(BEARER)) {
    throw new Unauthenticated("a bearer token is required");
  }
  return authorization.slice(BEARER.length);
}

/**
 * ADR-CA-008 · first, who is calling: the actor of a valid token, or the reason there is none. The refusal itself
 * waits for `requireAuthenticated`, so the rate limit counts the request first (ADR-CA-010, inspection m-9).
 */
export function identify(verifier: JoseTokenVerifier): RequestHandler {
  return async (request: Request, response: Response, next: NextFunction) => {
    try {
      response.locals.actor = await verifier.verify(bearerTokenOf(request.headers.authorization));
      next();
    } catch (error) {
      if (!(error instanceof Unauthenticated)) {
        next(error); // keys that cannot be fetched are ours, not the caller's
        return;
      }
      response.locals.unauthenticated = error;
      next();
    }
  };
}

/**
 * ADR-CA-008, ADR-CA-009 · deny by default: an unidentified caller gets 401 before the body is parsed or the
 * request is validated, so it learns nothing about the contract but "401".
 */
export function requireAuthenticated(): RequestHandler {
  return (_request: Request, response: Response, next: NextFunction) => {
    next(response.locals.unauthenticated);
  };
}

/** The authenticated caller of the current request (set by `authenticate`). */
export const CurrentActor = createParamDecorator(
  (_data: unknown, context: ExecutionContext): Actor => context.switchToHttp().getResponse<Response>().locals.actor as Actor,
);
