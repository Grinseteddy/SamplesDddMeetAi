package org.larder.cookingassistance;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.larder.cookingassistance.domain.CatastropheMitigation;
import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.Course;
import org.larder.cookingassistance.domain.HelpProviderType;
import org.larder.cookingassistance.domain.HelpRequestDraft;
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

/** The examples of the contracts and the visual glossaries. */
public final class TestData {

    public static final CookId COOK = new CookId(UUID.fromString("f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074"));
    public static final CookId OTHER_COOK = new CookId(UUID.fromString("39a7aed5-2e50-48c8-8aa6-f3afa9f03f74"));
    public static final RecipeId SCONES = new RecipeId(UUID.fromString("7cf09822-77a1-46bb-812f-b7852bca0913"));
    public static final RecipeId SOUP = new RecipeId(UUID.fromString("1b2c3d4e-5f60-4718-9a2b-3c4d5e6f7a81"));
    public static final HowToStepId FOLD_IN = new HowToStepId(UUID.fromString("65610dee-fb83-4341-a730-26a4a99a621a"));
    public static final IngredientId BUTTERMILK = new IngredientId(UUID.fromString("8956b2e5-8d0c-470e-ae9c-6071bd7c5b0d"));
    public static final IngredientId BUTTER = new IngredientId(UUID.fromString("497f6eca-6276-4993-bfeb-53cbbbba6f08"));
    public static final UUID HELP_REQUEST_ID = UUID.fromString("23a8eeed-35f6-460b-892e-7bb458a8fded");
    public static final UUID GRANDMA_HELP_ID = UUID.fromString("a9caf90d-00b4-4a66-8184-7d02152e8d6a");
    public static final Instant NOW = Instant.parse("2026-10-03T16:30:00Z");

    private TestData() {
    }

    public static Set<HelpProviderType> providers(HelpProviderType... providers) {
        return new LinkedHashSet<>(List.of(providers));
    }

    /** STEPS_TO_MITIGATE_CATASTROPHE with a recipe, answered by Grandma Avatar or community. */
    public static HelpRequestDraft burningCatastrophe() {
        return new HelpRequestDraft("Burning Catastrophe", HelpType.STEPS_TO_MITIGATE_CATASTROPHE,
                "Scones are burned and mother in law is coming in 30 minutes", SCONES, null, Set.of(),
                providers(HelpProviderType.GRANDMA_AVATAR, HelpProviderType.COMMUNITY));
    }

    public static HelpRequestDraft foldingTheDough() {
        return new HelpRequestDraft("Folding the dough", HelpType.PREPARATION_STEP_EXPLANATION,
                "What does \"fold in\" mean in step 3?", SCONES, FOLD_IN, Set.of(),
                providers(HelpProviderType.GRANDMA_AVATAR));
    }

    public static HelpRequestDraft noButtermilk() {
        return new HelpRequestDraft("No buttermilk", HelpType.INGREDIENT_SUBSTITUTE,
                "What can I use instead of buttermilk?", SCONES, null, Set.of(BUTTERMILK),
                providers(HelpProviderType.COMMUNITY));
    }

    public static HelpRequestDraft dinnerForTheInLaws() {
        return new HelpRequestDraft("Dinner for my in-laws", HelpType.MENU_PROPOSAL,
                "Six guests on Saturday, one of them vegetarian", null, null, Set.of(),
                providers(HelpProviderType.CHEF));
    }

    public static CatastropheMitigation stayCalm() {
        return new CatastropheMitigation(Optional.of(SCONES), "use a new, cold pan");
    }

    public static PreparationStepExplanation useAColdPan() {
        return new PreparationStepExplanation(SCONES, FOLD_IN, "Use a cold pan",
                List.of(URI.create("https://larder.org/media/images/b009a5d1-0205-4b0c-af82-822229cf243a")));
    }

    public static IngredientSubstitutes yoghurtForButtermilk() {
        return new IngredientSubstitutes(SCONES,
                List.of(new Substitute(BUTTERMILK, "Yoghurt", new BigDecimal("250"), Unit.MILLILITER)));
    }

    public static MenuProposal threeCourses() {
        return new MenuProposal("Be careful, prepare everything", 6, Meal.DINNER,
                Optional.of("hold course 2 warm while serving soup"),
                List.of(new Course(1, SOUP), new Course(2, SCONES), new Course(2, SOUP)));
    }
}
