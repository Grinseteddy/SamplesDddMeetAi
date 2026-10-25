import { Body, Controller, Get, HttpCode, Inject, Param, Post, Query, Res } from "@nestjs/common";
import type { Response } from "express";
import type { Actor } from "../../../application/actor.js";
import { HelpRequestNotFound } from "../../../application/queries/help-request-queries.js";
import { TopicNotYetSupported } from "../../../application/topic-not-yet-supported.js";
import { HelpRequestId } from "../../../domain/help-request/help-request.js";
import type { CookId } from "../../../domain/shared/ids.js";
import { CurrentActor } from "./authentication.js";
import { ensurePermitted } from "./authorization.js";
import { HTTP_SETTINGS, type HttpSettings } from "./http-settings.js";
import {
  type HelpRequestPageResource,
  type HelpRequestResource,
  type ListHelpRequestsQuery,
  type HelpRequestResponse,
  type RequestHelpBody,
  toCommand,
  toHelpRequestPage,
  toHelpRequestResource,
  toHelpRequestResponse,
} from "./request-help.mapper.js";

@Controller("help-requests")
export class HelpRequestsController {
  constructor(@Inject(HTTP_SETTINGS) private readonly settings: HttpSettings) {}

  // op: requestHelp (cook-assistance.openapi.yaml) · cmd: Request help preparation step
  @Post()
  @HttpCode(201)
  async requestHelp(
    @Body() body: RequestHelpBody,
    @CurrentActor() actor: Actor,
    @Res({ passthrough: true }) response: Response,
  ): Promise<HelpRequestResponse> {
    ensurePermitted(actor, "requestHelp");
    // dispatch by the topic discriminator: one use case per topic, Preparation step only in increment 0
    if (body.topic !== "PREPARATION_STEP") {
      throw new TopicNotYetSupported(body.topic);
    }
    const view = await this.settings.cookAssistance.requestPreparationStepHelp.execute(toCommand(body, actor));
    const created = toHelpRequestResponse(view, this.settings.publicBaseUrl);
    response.location(created.selfLink);
    return created;
  }

  // op: getHelpRequestById (cook-assistance.openapi.yaml) · RM: Help request
  @Get(":helpRequestId")
  async getHelpRequestById(@Param("helpRequestId") helpRequestId: string, @CurrentActor() actor: Actor): Promise<HelpRequestResource> {
    ensurePermitted(actor, "getHelpRequestById");
    const readModel = await this.settings.cookAssistance.helpRequestQueries.get(HelpRequestId.of(helpRequestId), actor);
    if (readModel === undefined) {
      throw new HelpRequestNotFound(helpRequestId);
    }
    return toHelpRequestResource(readModel, this.settings.publicBaseUrl);
  }

  // op: listHelpRequests (cook-assistance.openapi.yaml) · RM: Help request — parameters validated and defaulted by the spec
  @Get()
  async listHelpRequests(@Query() query: ListHelpRequestsQuery, @CurrentActor() actor: Actor): Promise<HelpRequestPageResource> {
    ensurePermitted(actor, "listHelpRequests");
    const page = await this.settings.cookAssistance.helpRequestQueries.list(
      // validated by the spec: enum values and a uuid
      { status: query.status, topic: query.topic, addressee: query.addressee, requester: query.requester as CookId | undefined },
      { number: query.page, size: query.size },
      actor,
    );
    return toHelpRequestPage(page, this.settings.publicBaseUrl);
  }
}
