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
 * ADR-CA-008, ADR-CA-009 · deny by default: the caller is authenticated before the body is parsed or the
 * request is validated, so a caller without a valid token learns nothing about the contract but "401".
 */
export function authenticate(verifier: JoseTokenVerifier): RequestHandler {
  return async (request: Request, response: Response, next: NextFunction) => {
    try {
      response.locals.actor = await verifier.verify(bearerTokenOf(request.headers.authorization));
      next();
    } catch (error) {
      next(error);
    }
  };
}

/** The authenticated caller of the current request (set by `authenticate`). */
export const CurrentActor = createParamDecorator(
  (_data: unknown, context: ExecutionContext): Actor => context.switchToHttp().getResponse<Response>().locals.actor as Actor,
);
