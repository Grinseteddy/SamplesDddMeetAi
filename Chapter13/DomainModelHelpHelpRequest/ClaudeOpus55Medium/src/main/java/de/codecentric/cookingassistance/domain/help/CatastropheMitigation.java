package de.codecentric.cookingassistance.domain.help;

import de.codecentric.cookingassistance.domain.external.RecipeId;
import de.codecentric.cookingassistance.domain.shared.AnswerType;
import de.codecentric.cookingassistance.domain.shared.Require;

import java.util.Objects;
import java.util.Optional;

/**
 * Glossary term "catastrophe mitigation" — the answer to a Steps to Mitigate
 * Catastrophe request. A value object (final class rather than record because
 * of the optional recipe).
 */
public final class CatastropheMitigation implements Answer {

    private final RecipeId recipe;      // "contains 0..1 recipe" — "Same recipe as in request"
    private final String explanation;   // "contains 1 explanation"

    public CatastropheMitigation(RecipeId recipe, String explanation) {
        this.recipe = recipe;
        this.explanation = Require.text(explanation, "explanation");
    }

    public static CatastropheMitigation withoutRecipe(String explanation) {
        return new CatastropheMitigation(null, explanation);
    }

    public Optional<RecipeId> recipe() {
        return Optional.ofNullable(recipe);
    }

    public String explanation() {
        return explanation;
    }

    @Override
    public AnswerType answerType() {
        return AnswerType.STEPS_TO_MITIGATE_CATASTROPHE;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CatastropheMitigation that
                && Objects.equals(recipe, that.recipe)
                && explanation.equals(that.explanation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipe, explanation);
    }

    @Override
    public String toString() {
        return "CatastropheMitigation[recipe=" + recipe + ", explanation=" + explanation + "]";
    }
}
