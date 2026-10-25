import { Controller, Get, Inject, Param } from "@nestjs/common";
import type { Actor } from "../../../application/actor.js";
import { HelpNotFound } from "../../../application/queries/help-queries.js";
import { HelpId } from "../../../domain/help/help.js";
import { CurrentActor } from "./authentication.js";
import { ensurePermitted } from "./authorization.js";
import { type HelpResource, toHelpResource } from "./help.mapper.js";
import { HTTP_SETTINGS, type HttpSettings } from "./http-settings.js";

@Controller("helps")
export class HelpsController {
  constructor(@Inject(HTTP_SETTINGS) private readonly settings: HttpSettings) {}

  // op: getHelpById (cook-assistance.openapi.yaml) · RM: Help
  @Get(":helpId")
  async getHelpById(@Param("helpId") helpId: string, @CurrentActor() actor: Actor): Promise<HelpResource> {
    ensurePermitted(actor, "getHelpById");
    const readModel = await this.settings.cookAssistance.helpQueries.get(HelpId.of(helpId), actor);
    if (readModel === undefined) {
      throw new HelpNotFound(helpId);
    }
    return toHelpResource(readModel, this.settings.publicBaseUrl);
  }
}
