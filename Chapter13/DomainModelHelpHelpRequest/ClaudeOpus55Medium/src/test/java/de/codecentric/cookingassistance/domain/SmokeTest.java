package de.codecentric.cookingassistance.domain;

import de.codecentric.cookingassistance.domain.external.CookId;
import de.codecentric.cookingassistance.domain.external.HowToStepId;
import de.codecentric.cookingassistance.domain.external.IngredientId;
import de.codecentric.cookingassistance.domain.external.RecipeId;
import de.codecentric.cookingassistance.domain.help.CatastropheMitigation;
import de.codecentric.cookingassistance.domain.help.Course;
import de.codecentric.cookingassistance.domain.help.Help;
import de.codecentric.cookingassistance.domain.help.HelpRuleViolation;
import de.codecentric.cookingassistance.domain.help.Meal;
import de.codecentric.cookingassistance.domain.help.MenuProposal;
import de.codecentric.cookingassistance.domain.help.PreparationStepExplanation;
import de.codecentric.cookingassistance.domain.help.Substitute;
import de.codecentric.cookingassistance.domain.help.SubstituteIngredient;
import de.codecentric.cookingassistance.domain.help.Substitutes;
import de.codecentric.cookingassistance.domain.help.Unit;
import de.codecentric.cookingassistance.domain.helprequest.HelpRequest;
import de.codecentric.cookingassistance.domain.helprequest.HelpRequestRuleViolation;
import de.codecentric.cookingassistance.domain.helprequest.Status;
import de.codecentric.cookingassistance.domain.shared.AnswerType;
import de.codecentric.cookingassistance.domain.shared.DomainRuleViolation;
import de.codecentric.cookingassistance.domain.shared.HelpAccepted;
import de.codecentric.cookingassistance.domain.shared.HelpId;
import de.codecentric.cookingassistance.domain.shared.HelpProviderType;
import de.codecentric.cookingassistance.domain.shared.HelpRequestId;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static de.codecentric.cookingassistance.domain.shared.AnswerType.*;
import static de.codecentric.cookingassistance.domain.shared.HelpProviderType.*;

/** Dependency-free smoke test: one valid case per aggregate, one violation per rule. */
public final class SmokeTest {

    // Example values taken from the glossary stickies.
    static final HelpRequestId REQUEST_ID = HelpRequestId.of("23a8eeed-35f6-460b-892e-7bb458a8fded");
    static final CookId REQUESTER = CookId.of("f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074");
    static final CookId OTHER_COOK = CookId.of("39a7aed5-2e50-48c8-8aa6-f3afa9f03f74");
    static final RecipeId RECIPE = RecipeId.of("7cf09822-77a1-46bb-812f-b7852bca0913");
    static final RecipeId OTHER_RECIPE = RecipeId.of("00000000-0000-4000-8000-000000000001");
    static final HowToStepId HOW_TO = HowToStepId.of("65610dee-fb83-4341-a730-26a4a99a621a");
    static final HowToStepId OTHER_HOW_TO = HowToStepId.of("00000000-0000-4000-8000-000000000002");
    static final IngredientId INGREDIENT = IngredientId.of("8956b2e5-8d0c-470e-ae9c-6071bd7c5b0d");
    static final IngredientId OTHER_INGREDIENT = IngredientId.of("00000000-0000-4000-8000-000000000003");
    static final URI PICTURE = URI.create("https://larder.org/media/images/b009a5d1-0205-4b0c-af82-822229cf243a");

    static int passed;

    public static void main(String[] args) {
        helpRequest();
        help();
        System.out.println("OK — " + passed + " checks passed");
    }

    // ---- Help Request ------------------------------------------------------

    static HelpRequest request(AnswerType type, RecipeId recipe, HowToStepId howTo,
                               List<IngredientId> ingredients, List<HelpProviderType> providers) {
        return HelpRequest.request(HelpRequestId.generate(), REQUESTER, "Burning Catastrophe", type,
                "Scones are burned and mother in law is coming in 30 minutes",
                recipe, howTo, ingredients, providers, List.of(PICTURE));
    }

    static void helpRequest() {
        HelpRequest valid = HelpRequest.request(REQUEST_ID, REQUESTER, "Burning Catastrophe",
                PREPARATION_STEP_EXPLANATION, "Scones are burned and mother in law is coming in 30 minutes",
                RECIPE, HOW_TO, List.of(), List.of(GRANDMA_AVATAR, COMMUNITY), List.of(PICTURE));
        check(valid.status() == Status.OPEN, "new request is Open");

        expect(DomainRuleViolation.RequiredTermMissing.class,
                () -> HelpRequest.request(HelpRequestId.generate(), REQUESTER, " ",
                        STEPS_TO_MITIGATE_CATASTROPHE, "d", null, null, List.of(), List.of(COMMUNITY), List.of()));
        expect(DomainRuleViolation.RequiredTermMissing.class,
                () -> HelpRequest.request(HelpRequestId.generate(), null, "t",
                        STEPS_TO_MITIGATE_CATASTROPHE, "d", null, null, List.of(), List.of(COMMUNITY), List.of()));
        expect(HelpRequestRuleViolation.RecipeMandatoryForType.class,
                () -> request(PREPARATION_STEP_EXPLANATION, null, HOW_TO, List.of(), List.of(COMMUNITY)));
        expect(HelpRequestRuleViolation.RecipeMandatoryForType.class,
                () -> request(INGREDIENT_SUBSTITUTE, null, null, List.of(INGREDIENT), List.of(COMMUNITY)));
        expect(HelpRequestRuleViolation.HowToStepOnlyForPreparationStepExplanation.class,
                () -> request(INGREDIENT_SUBSTITUTE, RECIPE, HOW_TO, List.of(INGREDIENT), List.of(COMMUNITY)));
        expect(HelpRequestRuleViolation.IngredientsOnlyForIngredientSubstitute.class,
                () -> request(PREPARATION_STEP_EXPLANATION, RECIPE, HOW_TO, List.of(INGREDIENT), List.of(COMMUNITY)));
        expect(HelpRequestRuleViolation.PreferredProviderCountOutOfRange.class,
                () -> request(STEPS_TO_MITIGATE_CATASTROPHE, null, null, List.of(), List.of()));
        expect(HelpRequestRuleViolation.PreferredProviderCountOutOfRange.class,
                () -> request(STEPS_TO_MITIGATE_CATASTROPHE, null, null, List.of(),
                        List.of(COMMUNITY, GRANDMA_AVATAR, CHEF)));
        expect(HelpRequestRuleViolation.DuplicatePreferredProvider.class,
                () -> request(STEPS_TO_MITIGATE_CATASTROPHE, null, null, List.of(), List.of(COMMUNITY, COMMUNITY)));
        expect(HelpRequestRuleViolation.ChefHelpMustBeExclusive.class,
                () -> request(MENU_PROPOSAL, null, null, List.of(), List.of(CHEF, COMMUNITY)));
        expect(HelpRequestRuleViolation.ChefSupportOnlyForMenuProposal.class,
                () -> request(STEPS_TO_MITIGATE_CATASTROPHE, null, null, List.of(), List.of(CHEF)));
        check(request(MENU_PROPOSAL, null, null, List.of(), List.of(CHEF)).isChefRequest(),
                "Chef alone for a menu proposal is allowed");

        HelpRequest closed = request(STEPS_TO_MITIGATE_CATASTROPHE, null, null, List.of(), List.of(COMMUNITY));
        closed.close();
        expect(HelpRequestRuleViolation.HelpRequestClosed.class, closed::close);
        expect(HelpRequestRuleViolation.HelpRequestClosed.class,
                () -> closed.acceptHelp(new HelpAccepted(HelpId.generate(), closed.helpRequestId())));
        expect(HelpRequestRuleViolation.AcceptedHelpForOtherRequest.class,
                () -> valid.acceptHelp(new HelpAccepted(HelpId.generate(), HelpRequestId.generate())));
        expect(UnsupportedOperationException.class, () -> valid.ingredients().add(INGREDIENT));
    }

    // ---- Help ----------------------------------------------------------------

    static PreparationStepExplanation explanation(RecipeId recipe, HowToStepId howTo) {
        return new PreparationStepExplanation(recipe, howTo, "Use a cold pan", List.of(PICTURE));
    }

    static Substitutes substitutes(RecipeId recipe, IngredientId ingredient) {
        return new Substitutes(recipe, List.of(new Substitute(ingredient,
                new SubstituteIngredient("Buttermilk", new BigDecimal("1"), Unit.CUP))));
    }

    static MenuProposal menu(int courses) {
        return new MenuProposal("Be careful, prepare everything", 6, Meal.DINNER,
                "hold course 2 warm while serving soup",
                Collections.nCopies(courses, new Course(2, RECIPE)));
    }

    static void help() {
        // Valid end-to-end: provide, accept, request becomes Answered, can still be answered.
        HelpRequest pse = request(PREPARATION_STEP_EXPLANATION, RECIPE, HOW_TO, List.of(), List.of(GRANDMA_AVATAR));
        Help help = Help.provide(HelpId.generate(), pse, "Stay calm", GRANDMA_AVATAR, null, explanation(RECIPE, HOW_TO));
        check(help.helpRequester().equals(REQUESTER), "help requester copied from request");
        pse.acceptHelp(help.accept(pse));
        check(help.isAccepted() && pse.status() == Status.ANSWERED, "accepted help answers the request");
        Help.provide(HelpId.generate(), pse, "Another idea", COMMUNITY, OTHER_COOK, explanation(RECIPE, HOW_TO));
        check(true, "answered request can still be answered");
        expect(HelpRuleViolation.HelpAlreadyAccepted.class, () -> help.accept(pse));

        expect(HelpRuleViolation.AnswerTypeNotAsRequested.class,
                () -> Help.provide(HelpId.generate(), pse, "Stay calm", COMMUNITY, null,
                        CatastropheMitigation.withoutRecipe("use a new, cold pan")));
        expect(HelpRuleViolation.RecipeNotAsInRequest.class,
                () -> Help.provide(HelpId.generate(), pse, "Stay calm", COMMUNITY, null, explanation(OTHER_RECIPE, HOW_TO)));
        expect(HelpRuleViolation.HowToStepNotAsInRequest.class,
                () -> Help.provide(HelpId.generate(), pse, "Stay calm", COMMUNITY, null, explanation(RECIPE, OTHER_HOW_TO)));

        HelpRequest substitute = request(INGREDIENT_SUBSTITUTE, RECIPE, null, List.of(INGREDIENT), List.of(COMMUNITY));
        Help.provide(HelpId.generate(), substitute, "Use buttermilk", COMMUNITY, OTHER_COOK, substitutes(RECIPE, INGREDIENT));
        expect(HelpRuleViolation.IngredientNotInRequest.class,
                () -> Help.provide(HelpId.generate(), substitute, "x", COMMUNITY, OTHER_COOK, substitutes(RECIPE, OTHER_INGREDIENT)));
        expect(HelpRuleViolation.RecipeNotAsInRequest.class,
                () -> Help.provide(HelpId.generate(), substitute, "x", COMMUNITY, OTHER_COOK, substitutes(OTHER_RECIPE, INGREDIENT)));
        expect(DomainRuleViolation.RequiredTermMissing.class, () -> new Substitutes(RECIPE, List.of()));
        expect(SubstituteIngredient.QuantityNotPositive.class,
                () -> new SubstituteIngredient("Buttermilk", BigDecimal.ZERO, Unit.CUP));

        HelpRequest catastrophe = request(STEPS_TO_MITIGATE_CATASTROPHE, RECIPE, null, List.of(), List.of(COMMUNITY));
        Help.provide(HelpId.generate(), catastrophe, "Stay calm", COMMUNITY, null,
                CatastropheMitigation.withoutRecipe("use a new, cold pan"));
        expect(HelpRuleViolation.RecipeNotAsInRequest.class,
                () -> Help.provide(HelpId.generate(), catastrophe, "Stay calm", COMMUNITY, null,
                        new CatastropheMitigation(OTHER_RECIPE, "use a new, cold pan")));

        // Self-answer: allowed in general, refused for Chef requests.
        Help.provide(HelpId.generate(), catastrophe, "I fixed it", COMMUNITY, REQUESTER,
                CatastropheMitigation.withoutRecipe("scraped the top off"));
        HelpRequest chef = request(MENU_PROPOSAL, null, null, List.of(), List.of(CHEF));
        Help.provide(HelpId.generate(), chef, "Three courses", CHEF, OTHER_COOK, menu(3));
        expect(HelpRuleViolation.CannotAnswerOwnChefRequest.class,
                () -> Help.provide(HelpId.generate(), chef, "Three courses", CHEF, REQUESTER, menu(3)));

        expect(MenuProposal.CourseCountOutOfRange.class, () -> menu(0));
        expect(MenuProposal.CourseCountOutOfRange.class, () -> menu(MenuProposal.MAX_COURSES + 1));
        check(menu(MenuProposal.MAX_COURSES).course().size() == 10, "ten courses with the same dish are allowed");
        expect(MenuProposal.ServingsNotPositive.class,
                () -> new MenuProposal("n", 0, Meal.LUNCH, null, List.of(new Course(1, RECIPE))));
        expect(Course.DishNotPositive.class, () -> new Course(0, RECIPE));

        // Closed request takes no more help.
        HelpRequest closed = request(STEPS_TO_MITIGATE_CATASTROPHE, null, null, List.of(), List.of(COMMUNITY));
        Help early = Help.provide(HelpId.generate(), closed, "Stay calm", COMMUNITY, null,
                CatastropheMitigation.withoutRecipe("use a new, cold pan"));
        closed.close();
        expect(HelpRuleViolation.HelpRequestClosed.class,
                () -> Help.provide(HelpId.generate(), closed, "Stay calm", COMMUNITY, null,
                        CatastropheMitigation.withoutRecipe("use a new, cold pan")));
        expect(HelpRuleViolation.HelpRequestClosed.class, () -> early.accept(closed));
        expect(HelpRuleViolation.HelpBelongsToOtherRequest.class, () -> early.accept(catastrophe));
    }

    // ---- tiny harness ----------------------------------------------------------

    static void check(boolean condition, String what) {
        if (!condition) {
            throw new AssertionError("FAILED: " + what);
        }
        passed++;
    }

    static void expect(Class<? extends RuntimeException> expected, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            if (expected.isInstance(e)) {
                passed++;
                return;
            }
            throw new AssertionError("expected " + expected.getSimpleName() + " but got " + e, e);
        }
        throw new AssertionError("expected " + expected.getSimpleName() + " but nothing was thrown");
    }
}
