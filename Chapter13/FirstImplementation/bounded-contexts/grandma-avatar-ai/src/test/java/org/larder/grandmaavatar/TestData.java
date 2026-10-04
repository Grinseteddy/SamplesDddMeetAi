package org.larder.grandmaavatar;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.larder.grandmaavatar.domain.CookId;
import org.larder.grandmaavatar.domain.HelpProviderType;
import org.larder.grandmaavatar.domain.HelpRequest;
import org.larder.grandmaavatar.domain.HelpRequestId;
import org.larder.grandmaavatar.domain.HelpType;
import org.larder.grandmaavatar.domain.HowToStepId;
import org.larder.grandmaavatar.domain.IngredientId;
import org.larder.grandmaavatar.domain.RecipeId;

/** The glossary's examples: the burning scones and their friends. */
public final class TestData {

    public static final HelpRequestId BURNING_CATASTROPHE = id("23a8eeed-35f6-460b-892e-7bb458a8fded");
    public static final CookId COOK = new CookId(UUID.fromString("f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074"));
    public static final RecipeId SCONES = new RecipeId(UUID.fromString("7cf09822-77a1-46bb-812f-b7852bca0913"));
    public static final RecipeId SOUP = new RecipeId(UUID.fromString("0a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d"));
    public static final HowToStepId FOLD_IN = new HowToStepId(UUID.fromString("65610dee-fb83-4341-a730-26a4a99a621a"));
    public static final HowToStepId BAKE = new HowToStepId(UUID.fromString("75610dee-fb83-4341-a730-26a4a99a621b"));
    public static final IngredientId BUTTERMILK = new IngredientId(UUID.fromString("497f6eca-6276-4993-bfeb-53cbbbba6f08"));
    public static final IngredientId BUTTER = new IngredientId(UUID.fromString("8956b2e5-8d0c-470e-ae9c-6071bd7c5b0d"));
    public static final UUID CORRELATION = UUID.fromString("23a8eeed-35f6-460b-892e-7bb458a8fded");

    private static final Set<HelpProviderType> GRANDMA_AND_COMMUNITY =
            Set.of(HelpProviderType.GRANDMA_AVATAR, HelpProviderType.COMMUNITY);

    private TestData() {
    }

    public static HelpRequest burningScones() {
        return new HelpRequest(BURNING_CATASTROPHE, COOK, "Burning Catastrophe", HelpType.STEPS_TO_MITIGATE_CATASTROPHE,
                "Scones are burned and mother in law is coming in 30 minutes", Optional.of(SCONES), Optional.empty(),
                List.of(), GRANDMA_AND_COMMUNITY, true);
    }

    public static HelpRequest catastropheWithoutRecipe() {
        return new HelpRequest(newId(), COOK, "Oh no", HelpType.STEPS_TO_MITIGATE_CATASTROPHE,
                "The soup is far too salty", Optional.empty(), Optional.empty(), List.of(), GRANDMA_AND_COMMUNITY, true);
    }

    public static HelpRequest foldingTheDough() {
        return new HelpRequest(newId(), COOK, "Folding the dough", HelpType.PREPARATION_STEP_EXPLANATION,
                "What does \"fold in\" mean in step 3?", Optional.of(SCONES), Optional.of(FOLD_IN), List.of(),
                Set.of(HelpProviderType.GRANDMA_AVATAR), true);
    }

    public static HelpRequest noButtermilk() {
        return new HelpRequest(newId(), COOK, "No buttermilk", HelpType.INGREDIENT_SUBSTITUTE,
                "What can I use instead of buttermilk and butter?", Optional.of(SCONES), Optional.empty(),
                List.of(BUTTERMILK, BUTTER), GRANDMA_AND_COMMUNITY, true);
    }

    public static HelpRequest dinnerForTheInLaws() {
        return new HelpRequest(newId(), COOK, "Dinner for my in-laws", HelpType.MENU_PROPOSAL,
                "Six guests on Saturday, one of them vegetarian", Optional.empty(), Optional.empty(), List.of(),
                Set.of(HelpProviderType.GRANDMA_AVATAR), true);
    }

    public static HelpRequest chefOnlyMenu() {
        return new HelpRequest(newId(), COOK, "Dinner for my in-laws", HelpType.MENU_PROPOSAL,
                "Six guests on Saturday, one of them vegetarian", Optional.empty(), Optional.empty(), List.of(),
                Set.of(HelpProviderType.CHEF), true);
    }

    public static HelpRequestId newId() {
        return new HelpRequestId(UUID.randomUUID());
    }

    private static HelpRequestId id(String value) {
        return new HelpRequestId(UUID.fromString(value));
    }
}
