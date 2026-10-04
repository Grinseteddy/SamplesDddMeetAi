package org.larder.cookingassistance.adapter.out.messaging;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.larder.cookingassistance.domain.Answer;
import org.larder.cookingassistance.domain.CatastropheMitigation;
import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.Help;
import org.larder.cookingassistance.domain.HelpRequestStatus;
import org.larder.cookingassistance.domain.IngredientSubstitutes;
import org.larder.cookingassistance.domain.MenuProposal;
import org.larder.cookingassistance.domain.PreparationStepExplanation;
import org.larder.cookingassistance.domain.RecipeId;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Payload of HelpProvided ({@code HelpProvidedPayload} in cooking-assistance.asyncapi.yaml). Unlike the
 * REST contract, the published language nests the answer body under a property named after its kind
 * ({@code substitutes}, {@code preparationStepExplanation}, {@code catastropheMitigation},
 * {@code menuProposal}), each repeating the {@code answerType}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
record HelpProvidedPayload(
        UUID helpId,
        UUID helpRequest,
        UUID helpRequester,
        String answerTitle,
        String helpProviderType,
        UUID helpProvider,
        AnswerPayload answer,
        String helpRequestStatus) {

    /** A help is only provided for a request that is thereby Answered. */
    static HelpProvidedPayload of(Help help) {
        return new HelpProvidedPayload(
                help.id().value(),
                help.helpRequest().value(),
                help.helpRequester().value(),
                help.answerTitle(),
                help.providerType().name(),
                help.helpProvider().map(CookId::value).orElse(null),
                AnswerPayload.of(help.answer()),
                HelpRequestStatus.ANSWERED.name());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record AnswerPayload(
            String answerType,
            SubstitutesPayload substitutes,
            PreparationStepExplanationPayload preparationStepExplanation,
            CatastropheMitigationPayload catastropheMitigation,
            MenuProposalPayload menuProposal) {

        static AnswerPayload of(Answer answer) {
            String type = answer.type().name();
            return switch (answer) {
                case IngredientSubstitutes s -> new AnswerPayload(type, new SubstitutesPayload(
                        s.recipe().value(),
                        s.substitutes().stream().map(substitute -> new SubstitutePayload(
                                substitute.ingredient().value(),
                                new SubstituteIngredientPayload(substitute.name(), substitute.value(),
                                        substitute.unit().name()))).toList(),
                        type), null, null, null);
                case PreparationStepExplanation p -> new AnswerPayload(type, null, new PreparationStepExplanationPayload(
                        p.recipe().value(), p.howToStep().value(), p.description(), p.images(), type), null, null);
                case CatastropheMitigation c -> new AnswerPayload(type, null, null, new CatastropheMitigationPayload(
                        c.recipe().map(RecipeId::value).orElse(null), c.explanation(), type), null);
                case MenuProposal m -> new AnswerPayload(type, null, null, null, new MenuProposalPayload(
                        m.note(), m.servings(), m.meal().name(), m.howToServe().orElse(null),
                        m.courses().stream().map(course -> new CoursePayload(course.step(),
                                new CourseMealPayload(course.recipe().value()))).toList(),
                        type));
            };
        }
    }

    record SubstitutesPayload(UUID recipe, List<SubstitutePayload> substitute, String answerType) {
    }

    record SubstitutePayload(UUID ingredient, SubstituteIngredientPayload substituteIngredient) {
    }

    record SubstituteIngredientPayload(String name, BigDecimal value, String unit) {
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    record PreparationStepExplanationPayload(UUID recipe, UUID howToStep, String description, List<URI> images,
                                             String answerType) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record CatastropheMitigationPayload(UUID recipe, String explanation, String answerType) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record MenuProposalPayload(String note, int servings, String meal, String howToServe, List<CoursePayload> course,
                               String answerType) {
    }

    record CoursePayload(int step, CourseMealPayload meal) {
    }

    record CourseMealPayload(UUID recipe) {
    }
}
