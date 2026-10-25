import { Body, Controller, HttpCode, Inject, Post, Res } from "@nestjs/common";
import type { Response } from "express";
import type { Actor } from "../../../application/actor.js";
import { TopicNotYetSupported } from "../../../application/topic-not-yet-supported.js";
import { CurrentActor } from "./authentication.js";
import { ensurePermitted } from "./authorization.js";
import { HTTP_SETTINGS, type HttpSettings } from "./http-settings.js";
import { type HelpRequestResponse, type RequestHelpBody, toCommand, toHelpRequestResponse } from "./request-help.mapper.js";

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
}
