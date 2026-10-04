package org.larder.grandmaavatar.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A cook's Help Request as Grandma sees it - a snapshot of the published language of Cooking
 * Assistance, checked against the invariants of the Help Request glossary:
 * <ul>
 *   <li>recipe is mandatory for {@code PREPARATION_STEP_EXPLANATION} and {@code INGREDIENT_SUBSTITUTE};</li>
 *   <li>howToStep is mandatory for, and only allowed with, {@code PREPARATION_STEP_EXPLANATION};</li>
 *   <li>ingredients are mandatory for, and only allowed with, {@code INGREDIENT_SUBSTITUTE};</li>
 *   <li>1..2 distinct preferred providers; {@code CHEF} only for {@code MENU_PROPOSAL} and then exclusively.</li>
 * </ul>
 * The glossary's "only for" rules are applied to every type, also to {@code MENU_PROPOSAL}, where the
 * JSON schema of the contract does not encode them. For {@code INGREDIENT_SUBSTITUTE} the contract
 * requires the ingredients but allows an empty list (no {@code minItems}); such a request is valid,
 * Grandma just has nothing to substitute. Preferred providers are a set: the contract demands
 * {@code uniqueItems}, so the inbound adapter rejects repetitions before they get here.
 */
public record HelpRequest(
        HelpRequestId id,
        CookId requester,
        String title,
        HelpType type,
        String description,
        Optional<RecipeId> recipe,
        Optional<HowToStepId> howToStep,
        List<IngredientId> ingredients,
        Set<HelpProviderType> preferredProviders,
        boolean open) {

    public HelpRequest {
        require(id != null, "helpRequestId is missing");
        require(requester != null, "requester is missing");
        require(type != null, "type is missing");
        Texts.requestText(title, "title", 200);
        Texts.requestText(description, "description", 2000);
        recipe = Objects.requireNonNullElse(recipe, Optional.empty());
        howToStep = Objects.requireNonNullElse(howToStep, Optional.empty());
        ingredients = ingredients == null ? List.of() : List.copyOf(ingredients);
        preferredProviders = preferredProviders == null ? Set.of() : Set.copyOf(preferredProviders);

        boolean explainsAStep = type == HelpType.PREPARATION_STEP_EXPLANATION;
        boolean substitutes = type == HelpType.INGREDIENT_SUBSTITUTE;
        require(!(explainsAStep || substitutes) || recipe.isPresent(), "a recipe is mandatory for " + type);
        require(explainsAStep == howToStep.isPresent(),
                explainsAStep ? "a howToStep is mandatory for " + type : "a howToStep is only allowed for PREPARATION_STEP_EXPLANATION");
        require(substitutes || ingredients.isEmpty(), "ingredients are only allowed for INGREDIENT_SUBSTITUTE");
        require(!preferredProviders.isEmpty() && preferredProviders.size() <= 2, "1 or 2 preferred providers are required");
        require(!preferredProviders.contains(HelpProviderType.CHEF)
                        || (type == HelpType.MENU_PROPOSAL && preferredProviders.size() == 1),
                "a chef only answers menu proposals, and then exclusively");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new InvalidHelpRequestException(message);
        }
    }

    /**
     * Grandma answers a request only while it is open and only when the cook named her among the
     * preferred providers. A request naming only {@code CHEF} (or only the community) is none of her
     * business - chef help is provided exclusively.
     */
    public boolean isForGrandma() {
        return open && preferredProviders.contains(HelpProviderType.GRANDMA_AVATAR);
    }
}
