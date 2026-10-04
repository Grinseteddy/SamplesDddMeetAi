package org.larder.cookingassistance.domain;

import java.util.Set;

/**
 * What a cook asks for when raising a help request. {@code recipe} and {@code howToStep} may be
 * {@code null}, {@code ingredients} may be empty; the type decides what is allowed.
 */
public record HelpRequestDraft(
        String title,
        HelpType type,
        String description,
        RecipeId recipe,
        HowToStepId howToStep,
        Set<IngredientId> ingredients,
        Set<HelpProviderType> preferredProviders) {
}
