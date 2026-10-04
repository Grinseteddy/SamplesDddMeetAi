package org.larder.cookingassistance.adapter.in.messaging;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.larder.cookingassistance.application.GrandmaHelp;
import org.larder.cookingassistance.domain.Answer;
import org.larder.cookingassistance.domain.CatastropheMitigation;
import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.Course;
import org.larder.cookingassistance.domain.HelpId;
import org.larder.cookingassistance.domain.HelpRequestId;
import org.larder.cookingassistance.domain.HelpType;
import org.larder.cookingassistance.domain.HowToStepId;
import org.larder.cookingassistance.domain.IngredientId;
import org.larder.cookingassistance.domain.IngredientSubstitutes;
import org.larder.cookingassistance.domain.Meal;
import org.larder.cookingassistance.domain.MenuProposal;
import org.larder.cookingassistance.domain.PreparationStepExplanation;
import org.larder.cookingassistance.domain.RecipeId;
import org.larder.cookingassistance.domain.Substitute;
import org.larder.cookingassistance.domain.Unit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payload of the Grandma Avatar's HelpProvided ({@code HelpProvidedPayload} in grandma-avatar.asyncapi.yaml;
 * the answer is the published language of Cooking Assistance). A tolerant reader: unknown properties
 * (e.g. the {@code answerType} repeated inside each answer body) are ignored, every rule is checked by
 * the domain after {@link #toGrandmaHelp()}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record GrandmaHelpProvidedPayload(
        UUID helpId,
        UUID helpRequest,
        UUID helpRequester,
        String answerTitle,
        String helpProviderType,
        UUID helpProvider,
        AnswerPayload answer) {

    static final String GRANDMA_AVATAR = "GRANDMA_AVATAR";

    /** @throws IllegalArgumentException if the message is incomplete or not the avatar's */
    GrandmaHelp toGrandmaHelp() {
        if (helpId == null || helpRequest == null || helpRequester == null || answer == null) {
            throw new IllegalArgumentException("helpId, helpRequest, helpRequester and answer are required");
        }
        if (!GRANDMA_AVATAR.equals(helpProviderType) || helpProvider != null) {
            throw new IllegalArgumentException("Only helps of the Grandma Avatar, without helpProvider, are consumed here; got "
                    + helpProviderType);
        }
        return new GrandmaHelp(new HelpId(helpId), new HelpRequestId(helpRequest), new CookId(helpRequester),
                answerTitle, answer.toDomain());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AnswerPayload(
            String answerType,
            SubstitutesPayload substitutes,
            PreparationStepExplanationPayload preparationStepExplanation,
            CatastropheMitigationPayload catastropheMitigation,
            MenuProposalPayload menuProposal) {

        Answer toDomain() {
            HelpType type = HelpType.valueOf(String.valueOf(answerType));
            return switch (type) {
                case INGREDIENT_SUBSTITUTE -> required(substitutes, type).toDomain();
                case PREPARATION_STEP_EXPLANATION -> required(preparationStepExplanation, type).toDomain();
                case STEPS_TO_MITIGATE_CATASTROPHE -> required(catastropheMitigation, type).toDomain();
                case MENU_PROPOSAL -> required(menuProposal, type).toDomain();
            };
        }

        private <T> T required(T body, HelpType type) {
            long bodies = Stream.of(substitutes, preparationStepExplanation, catastropheMitigation,
                    menuProposal).filter(Objects::nonNull).count();
            if (body == null || bodies != 1) {
                throw new IllegalArgumentException("An answer of type " + type + " carries exactly its own answer body");
            }
            return body;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SubstitutesPayload(UUID recipe, List<SubstitutePayload> substitute) {

        Answer toDomain() {
            return new IngredientSubstitutes(recipe == null ? null : new RecipeId(recipe),
                    substitute == null ? null : substitute.stream().map(SubstitutePayload::toDomain).toList());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SubstitutePayload(UUID ingredient, SubstituteIngredientPayload substituteIngredient) {

        Substitute toDomain() {
            if (ingredient == null || substituteIngredient == null) {
                throw new IllegalArgumentException("A substitute names the ingredient and its substitute");
            }
            return new Substitute(new IngredientId(ingredient), substituteIngredient.name(), substituteIngredient.value(),
                    substituteIngredient.unit() == null ? null : Unit.valueOf(substituteIngredient.unit()));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SubstituteIngredientPayload(String name, BigDecimal value, String unit) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PreparationStepExplanationPayload(UUID recipe, UUID howToStep, String description, List<URI> images) {

        Answer toDomain() {
            return new PreparationStepExplanation(recipe == null ? null : new RecipeId(recipe),
                    howToStep == null ? null : new HowToStepId(howToStep), description, images);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CatastropheMitigationPayload(UUID recipe, String explanation) {

        Answer toDomain() {
            return new CatastropheMitigation(Optional.ofNullable(recipe).map(RecipeId::new), explanation);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MenuProposalPayload(String note, Integer servings, String meal, String howToServe, List<CoursePayload> course) {

        Answer toDomain() {
            if (servings == null || meal == null || course == null) {
                throw new IllegalArgumentException("A menu proposal has servings, a meal and courses");
            }
            return new MenuProposal(note, servings, Meal.valueOf(meal), Optional.ofNullable(howToServe),
                    course.stream().map(CoursePayload::toDomain).toList());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CoursePayload(Integer step, CourseMealPayload meal) {

        Course toDomain() {
            if (step == null || meal == null || meal.recipe() == null) {
                throw new IllegalArgumentException("A course has a step and the recipe of its dish");
            }
            return new Course(step, new RecipeId(meal.recipe()));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CourseMealPayload(UUID recipe) {
    }
}
