package org.larder.cookingassistance.domain;

import static org.larder.cookingassistance.domain.HelpRuleViolationException.INVALID_ANSWER;

import java.net.URI;
import java.util.List;

/** Answer of type Preparation Step Explanation: how to carry out the unclear step, optionally with pictures. */
public record PreparationStepExplanation(RecipeId recipe, HowToStepId howToStep, String description, List<URI> images)
        implements Answer {

    public PreparationStepExplanation {
        if (recipe == null || howToStep == null) {
            throw new HelpRuleViolationException(INVALID_ANSWER, "A preparation step explanation names the recipe and the step");
        }
        description = Texts.required(description, Texts.LONG_TEXT, INVALID_ANSWER, "The explanation of the step");
        images = images == null ? List.of() : List.copyOf(images);
    }

    @Override
    public HelpType type() {
        return HelpType.PREPARATION_STEP_EXPLANATION;
    }
}
