package org.larder.grandmaavatar.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Substitutes for the ingredients of a recipe. Same recipe as the request, and exactly one substitute
 * for every ingredient the cook asked about - no more, no less.
 */
public record Substitutes(RecipeId recipe, List<Substitute> substitutes) implements Answer {

    public Substitutes {
        Answer.require(recipe != null, "substitutes need the recipe");
        substitutes = substitutes == null ? List.of() : List.copyOf(substitutes);
        Answer.require(!substitutes.isEmpty(), "at least one substitute is required");
    }

    @Override
    public HelpType type() {
        return HelpType.INGREDIENT_SUBSTITUTE;
    }

    @Override
    public void mustFit(HelpRequest request) {
        Answer.require(request.recipe().equals(java.util.Optional.of(recipe)), "substitutes must be for the requested recipe");
        Set<IngredientId> substituted = new HashSet<>();
        for (Substitute substitute : substitutes) {
            Answer.require(substituted.add(substitute.ingredient()), "one substitute per ingredient");
        }
        Answer.require(substituted.equals(Set.copyOf(request.ingredients())),
                "exactly the requested ingredients must be substituted");
    }

    /** One ingredient of the recipe and what to use instead. */
    public record Substitute(IngredientId ingredient, SubstituteIngredient substituteIngredient) {

        public Substitute {
            Objects.requireNonNull(ingredient, "ingredient");
            Objects.requireNonNull(substituteIngredient, "substituteIngredient");
        }
    }
}
