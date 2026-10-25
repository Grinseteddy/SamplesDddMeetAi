import type { GrandmaAvatarHelpProvidedPayload } from "../../../../generated/asyncapi/cook-assistance.js";
import { MessageId } from "../../../application/ports/processed-messages.js";
import type { GrandmaAvatarAnswered } from "../../../application/provide-help-when-grandma-avatar-answers.js";
import { TopicNotYetSupported } from "../../../application/topic-not-yet-supported.js";
import { HelpRequestId } from "../../../domain/help-request/help-request.js";
import { HelpId } from "../../../domain/help/help.js";
import { HowToStepId, RecipeId } from "../../../domain/shared/ids.js";

/** Their answer types → our topics (AsyncAPI 2.0.0 r2, GrandmaAvatarAnswer). */
const TOPIC_OF_ANSWER_TYPE = {
  MENU_PROPOSAL: "MENU",
  PREPARATION_STEP_EXPLANATION: "PREPARATION_STEP",
  INGREDIENT_SUBSTITUTE: "INGREDIENTS",
  STEPS_TO_MITIGATE_CATASTROPHE: "CATASTROPHE",
} as const;

// Anticorruption layer (context map: Grandma Avatar AI → Cook Assistance): their published language → ours.
export function toGrandmaAvatarAnswered(messageId: string, payload: GrandmaAvatarHelpProvidedPayload): GrandmaAvatarAnswered {
  const answer = payload.answer;
  if (answer.answerType !== "PREPARATION_STEP_EXPLANATION") {
    throw new TopicNotYetSupported(TOPIC_OF_ANSWER_TYPE[answer.answerType]);
  }
  return {
    messageId: MessageId.of(messageId),
    helpId: HelpId.of(payload.helpId),
    helpRequestId: HelpRequestId.of(payload.helpRequest),
    answerTitle: payload.answerTitle,
    answer: {
      topic: TOPIC_OF_ANSWER_TYPE[answer.answerType],
      recipeId: RecipeId.of(answer.recipeId),
      howToStepId: HowToStepId.of(answer.howToStepId),
      description: answer.description,
      images: answer.images ?? [], // the spec's default (AsyncAPI 2.0.0 r2)
    },
  };
}
