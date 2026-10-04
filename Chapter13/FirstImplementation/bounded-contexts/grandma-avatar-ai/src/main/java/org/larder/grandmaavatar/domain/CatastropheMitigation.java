package org.larder.grandmaavatar.domain;

import java.util.Objects;
import java.util.Optional;

/** What to do now that the scones are burning. The recipe, if named, is the one of the request. */
public record CatastropheMitigation(Optional<RecipeId> recipe, String explanation) implements Answer {

    public CatastropheMitigation {
        recipe = Objects.requireNonNullElse(recipe, Optional.empty());
        Texts.answerText(explanation, "explanation", 2000);
    }

    @Override
    public HelpType type() {
        return HelpType.STEPS_TO_MITIGATE_CATASTROPHE;
    }

    @Override
    public void mustFit(HelpRequest request) {
        Answer.require(recipe.isEmpty() || recipe.equals(request.recipe()),
                "a mitigation may only name the requested recipe");
    }
}
