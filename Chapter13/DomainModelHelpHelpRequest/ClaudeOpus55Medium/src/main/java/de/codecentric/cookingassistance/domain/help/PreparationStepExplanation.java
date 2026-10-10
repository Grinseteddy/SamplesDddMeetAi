package de.codecentric.cookingassistance.domain.help;

import de.codecentric.cookingassistance.domain.external.HowToStepId;
import de.codecentric.cookingassistance.domain.external.RecipeId;
import de.codecentric.cookingassistance.domain.shared.AnswerType;
import de.codecentric.cookingassistance.domain.shared.Require;

import java.net.URI;
import java.util.List;

/**
 * Glossary term "preparation step explanation".
 *
 * @param recipe      "contains 1 recipe" — "Same recipe as in request" (checked by {@link Help})
 * @param howToStep   "contains 1 howTo Step" — "Same howTo as in request" (checked by {@link Help})
 * @param description "contains 1 description"
 * @param images      "contains 0..* images"
 */
public record PreparationStepExplanation(RecipeId recipe,
                                         HowToStepId howToStep,
                                         String description,
                                         List<URI> images) implements Answer {

    public PreparationStepExplanation {
        Require.present(recipe, "recipe");
        Require.present(howToStep, "howTo Step");
        Require.text(description, "description");
        images = Require.list(images, "images");
    }

    @Override
    public AnswerType answerType() {
        return AnswerType.PREPARATION_STEP_EXPLANATION;
    }
}
