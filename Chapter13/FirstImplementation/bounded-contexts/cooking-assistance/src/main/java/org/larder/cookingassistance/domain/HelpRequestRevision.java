package org.larder.cookingassistance.domain;

import java.util.Set;

/**
 * A partial change of a help request. {@code null} means "leave as it is" - also for the
 * collections; the type of a request cannot be changed.
 */
public record HelpRequestRevision(
        String title,
        String description,
        RecipeId recipe,
        HowToStepId howToStep,
        Set<IngredientId> ingredients,
        Set<HelpProviderType> preferredProviders,
        HelpRequestStatus status) {

    public static HelpRequestRevision none() {
        return new HelpRequestRevision(null, null, null, null, null, null, null);
    }

    public HelpRequestRevision withTitle(String newTitle) {
        return new HelpRequestRevision(newTitle, description, recipe, howToStep, ingredients, preferredProviders, status);
    }

    public HelpRequestRevision withRecipe(RecipeId newRecipe) {
        return new HelpRequestRevision(title, description, newRecipe, howToStep, ingredients, preferredProviders, status);
    }

    public HelpRequestRevision withHowToStep(HowToStepId newHowToStep) {
        return new HelpRequestRevision(title, description, recipe, newHowToStep, ingredients, preferredProviders, status);
    }

    public HelpRequestRevision withIngredients(Set<IngredientId> newIngredients) {
        return new HelpRequestRevision(title, description, recipe, howToStep, newIngredients, preferredProviders, status);
    }

    public HelpRequestRevision withPreferredProviders(Set<HelpProviderType> newProviders) {
        return new HelpRequestRevision(title, description, recipe, howToStep, ingredients, newProviders, status);
    }

    public HelpRequestRevision withStatus(HelpRequestStatus newStatus) {
        return new HelpRequestRevision(title, description, recipe, howToStep, ingredients, preferredProviders, newStatus);
    }
}
