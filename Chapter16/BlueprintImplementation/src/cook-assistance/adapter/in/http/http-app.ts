import "reflect-metadata";
import { fileURLToPath } from "node:url";
import { type DynamicModule, type INestApplication, Module } from "@nestjs/common";
import { NestFactory } from "@nestjs/core";
import express from "express";
import helmet from "helmet";
import * as OpenApiValidator from "express-openapi-validator";
import { identify, requireAuthenticated } from "./authentication.js";
import { createRateLimiter, MAX_BODY_MEGABYTES } from "./edge-limits.js";
import { HelpRequestsController } from "./help-requests.controller.js";
import { HelpsController } from "./helps.controller.js";
import { HTTP_SETTINGS, type HttpSettings } from "./http-settings.js";
import { ProblemFilter, problemErrorHandlerFor } from "./problem.js";

const API_SPEC = fileURLToPath(new URL("../../../../../contracts/openapi/cook-assistance.openapi.yaml", import.meta.url));

/** The path of the spec's server (https://larder.org/cooking-assistance): the service serves exactly the spec's paths. */
const SERVER_PATH = "cooking-assistance";

@Module({})
export class CookAssistanceHttpModule {
  static with(settings: HttpSettings): DynamicModule {
    return {
      module: CookAssistanceHttpModule,
      controllers: [HelpRequestsController, HelpsController],
      providers: [{ provide: HTTP_SETTINGS, useValue: settings }],
    };
  }
}

/** The HTTP adapter as one Nest application; tests drive it in process, main.ts listens on a port. */
export async function createHttpApp(settings: HttpSettings): Promise<INestApplication> {
  const app = await NestFactory.create(CookAssistanceHttpModule.with(settings), { bodyParser: false, logger: false });
  app.setGlobalPrefix(SERVER_PATH);
  // ADR-CA-010 · security headers on every answer, 401s included; a JSON API loads nothing (CSP default-src 'none')
  app.use(helmet({ contentSecurityPolicy: { useDefaults: false, directives: { defaultSrc: ["'none'"] } } }));
  // ADR-CA-008, ADR-CA-010 · identify, count, then refuse: every request counts against the limit (inspection m-9)
  app.use(identify(settings.tokenVerifier));
  app.use(createRateLimiter().middleware);
  app.use(requireAuthenticated());
  app.use(express.json({ type: ["application/json", "application/*+json"], limit: `${MAX_BODY_MEGABYTES}mb` }));
  // ADR-CA-011 · the spec is enforced at the edge: requests before any mapping, responses before they leave
  app.use(OpenApiValidator.middleware({ apiSpec: API_SPEC, validateRequests: true, validateResponses: true, validateSecurity: false }));
  app.useGlobalFilters(new ProblemFilter(settings.log));
  await app.init();
  app.use(problemErrorHandlerFor(settings.log));
  return app;
}
