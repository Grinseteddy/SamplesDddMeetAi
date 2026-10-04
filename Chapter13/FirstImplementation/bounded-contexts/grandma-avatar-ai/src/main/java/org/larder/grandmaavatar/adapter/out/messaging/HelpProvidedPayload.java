package org.larder.grandmaavatar.adapter.out.messaging;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Payload of {@code HelpProvided} exactly as {@code grandma-avatar.asyncapi.yaml}
 * ({@code HelpProvidedPayload}, answer from the published language of Cooking Assistance) defines it.
 * No {@code helpProvider} (the avatar is no cook) and no {@code helpRequestStatus} (that is Cooking
 * Assistance's to decide).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HelpProvidedPayload(
        UUID helpId,
        UUID helpRequest,
        UUID helpRequester,
        String answerTitle,
        String helpProviderType,
        AnswerPayload answer) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AnswerPayload(
            String answerType,
            SubstitutesPayload substitutes,
            PreparationStepExplanationPayload preparationStepExplanation,
            CatastropheMitigationPayload catastropheMitigation,
            MenuProposalPayload menuProposal) {
    }

    public record SubstitutesPayload(UUID recipe, List<SubstitutePayload> substitute, String answerType) {
    }

    public record SubstitutePayload(UUID ingredient, SubstituteIngredientPayload substituteIngredient) {
    }

    public record SubstituteIngredientPayload(String name, BigDecimal value, String unit) {
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record PreparationStepExplanationPayload(UUID recipe, UUID howToStep, String description, List<String> images,
                                                    String answerType) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CatastropheMitigationPayload(UUID recipe, String explanation, String answerType) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MenuProposalPayload(String note, int servings, String meal, String howToServe,
                                      List<CoursePayload> course, String answerType) {
    }

    public record CoursePayload(int step, CourseMealPayload meal) {
    }

    public record CourseMealPayload(UUID recipe) {
    }
}
