package org.larder.cookingassistance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.cookingassistance.TestData.BUTTER;
import static org.larder.cookingassistance.TestData.BUTTERMILK;
import static org.larder.cookingassistance.TestData.COOK;
import static org.larder.cookingassistance.TestData.FOLD_IN;
import static org.larder.cookingassistance.TestData.NOW;
import static org.larder.cookingassistance.TestData.OTHER_COOK;
import static org.larder.cookingassistance.TestData.SCONES;
import static org.larder.cookingassistance.TestData.SOUP;
import static org.larder.cookingassistance.TestData.burningCatastrophe;
import static org.larder.cookingassistance.TestData.dinnerForTheInLaws;
import static org.larder.cookingassistance.TestData.foldingTheDough;
import static org.larder.cookingassistance.TestData.noButtermilk;
import static org.larder.cookingassistance.TestData.stayCalm;
import static org.larder.cookingassistance.TestData.threeCourses;
import static org.larder.cookingassistance.TestData.useAColdPan;
import static org.larder.cookingassistance.TestData.yoghurtForButtermilk;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

class HelpTest {

    private static void assertViolates(String code, ThrowingCallable action) {
        assertThatThrownBy(action).isInstanceOf(HelpRuleViolationException.class)
                .satisfies(e -> assertThat(((HelpRuleViolationException) e).code()).isEqualTo(code));
    }

    private static HelpRequest raised(HelpRequestDraft draft) {
        return HelpRequest.raise(COOK, draft, NOW);
    }

    private static Help community(HelpRequest request, Answer answer) {
        return request.answer(HelpId.newId(), HelpProviderType.COMMUNITY, OTHER_COOK, "Try this", answer, NOW);
    }

    @Test
    void aHelpAnswersTheRequestAndCopiesTheRequester() {
        HelpRequest request = raised(burningCatastrophe());
        HelpId id = HelpId.newId();

        Help help = request.answer(id, HelpProviderType.GRANDMA_AVATAR, null, "Stay calm", stayCalm(), NOW.plusSeconds(60));

        assertThat(request.status()).isEqualTo(HelpRequestStatus.ANSWERED);
        assertThat(help.id()).isEqualTo(id);
        assertThat(help.helpRequest()).isEqualTo(request.id());
        assertThat(help.helpRequester()).isEqualTo(COOK);
        assertThat(help.helpProvider()).isEmpty();
        assertThat(help.answer()).isEqualTo(stayCalm());
        assertThat(help.createdAt()).isEqualTo(NOW.plusSeconds(60));
    }

    @Test
    void theAnswerTypeIsTheRequestType() {
        assertViolates(HelpRuleViolationException.ANSWER_TYPE_MISMATCH,
                () -> community(raised(burningCatastrophe()), useAColdPan()));
    }

    @Test
    void aStepExplanationIsAboutTheRequestedRecipeAndStep() {
        assertThatCode(() -> raised(foldingTheDough()).answer(HelpId.newId(), HelpProviderType.GRANDMA_AVATAR, null,
                "Cold pan", useAColdPan(), NOW)).doesNotThrowAnyException();
        HelpRequest request = raised(foldingTheDough());
        assertViolates(HelpRuleViolationException.ANSWER_REFERENCE_MISMATCH, () -> request.answer(HelpId.newId(),
                HelpProviderType.GRANDMA_AVATAR, null, "Cold pan",
                new PreparationStepExplanation(SOUP, FOLD_IN, "Use a cold pan", List.of()), NOW));
        assertViolates(HelpRuleViolationException.ANSWER_REFERENCE_MISMATCH, () -> request.answer(HelpId.newId(),
                HelpProviderType.GRANDMA_AVATAR, null, "Cold pan",
                new PreparationStepExplanation(SCONES, new HowToStepId(UUID.randomUUID()), "Use a cold pan", List.of()), NOW));
    }

    @Test
    void substitutesNameEachRequestedIngredientOfTheRecipeExactlyOnce() {
        assertThatCode(() -> community(raised(noButtermilk()), yoghurtForButtermilk())).doesNotThrowAnyException();
        Substitute yoghurt = yoghurtForButtermilk().substitutes().getFirst();
        Substitute margarine = new Substitute(BUTTER, "Margarine", BigDecimal.TEN, Unit.GRAM);

        assertViolates(HelpRuleViolationException.ANSWER_REFERENCE_MISMATCH,
                () -> community(raised(noButtermilk()), new IngredientSubstitutes(SOUP, List.of(yoghurt))));
        assertViolates(HelpRuleViolationException.ANSWER_REFERENCE_MISMATCH,
                () -> community(raised(noButtermilk()), new IngredientSubstitutes(SCONES, List.of(margarine))));
        assertViolates(HelpRuleViolationException.ANSWER_REFERENCE_MISMATCH,
                () -> community(raised(noButtermilk()), new IngredientSubstitutes(SCONES, List.of(yoghurt, yoghurt))));
        assertViolates(HelpRuleViolationException.ANSWER_REFERENCE_MISMATCH,
                () -> community(raised(noButtermilk()), new IngredientSubstitutes(SCONES, List.of(yoghurt, margarine))));
    }

    @Test
    void aCatastropheMitigationNamesNoOtherRecipeThanTheRequest() {
        assertThatCode(() -> community(raised(burningCatastrophe()),
                new CatastropheMitigation(Optional.empty(), "Open the window"))).doesNotThrowAnyException();
        assertViolates(HelpRuleViolationException.ANSWER_REFERENCE_MISMATCH, () -> community(raised(burningCatastrophe()),
                new CatastropheMitigation(Optional.of(SOUP), "Make soup instead")));
        HelpRequestDraft withoutRecipe = new HelpRequestDraft("Smoke", HelpType.STEPS_TO_MITIGATE_CATASTROPHE,
                "The kitchen is full of smoke", null, null, Set.of(), burningCatastrophe().preferredProviders());
        assertViolates(HelpRuleViolationException.ANSWER_REFERENCE_MISMATCH,
                () -> community(raised(withoutRecipe), stayCalm()));
    }

    @Test
    void onlyAPreferredProviderAnswers() {
        assertViolates(HelpRuleViolationException.PROVIDER_NOT_PREFERRED, () -> raised(burningCatastrophe())
                .answer(HelpId.newId(), HelpProviderType.CHEF, OTHER_COOK, "Chef says", stayCalm(), NOW));
        assertViolates(HelpRuleViolationException.PROVIDER_NOT_PREFERRED, () -> raised(dinnerForTheInLaws())
                .answer(HelpId.newId(), HelpProviderType.GRANDMA_AVATAR, null, "Grandma says", threeCourses(), NOW));
        assertThatCode(() -> raised(dinnerForTheInLaws())
                .answer(HelpId.newId(), HelpProviderType.CHEF, OTHER_COOK, "Chef says", threeCourses(), NOW))
                .doesNotThrowAnyException();
    }

    @Test
    void aCookAnswersTheirOwnRequestExceptAChefRequest() {
        assertThatCode(() -> raised(burningCatastrophe())
                .answer(HelpId.newId(), HelpProviderType.COMMUNITY, COOK, "Found it myself", stayCalm(), NOW))
                .doesNotThrowAnyException();
        assertViolates(HelpRuleViolationException.OWN_CHEF_REQUEST, () -> raised(dinnerForTheInLaws())
                .answer(HelpId.newId(), HelpProviderType.CHEF, COOK, "My own menu", threeCourses(), NOW));
    }

    @Test
    void onlyTheGrandmaAvatarAnswersWithoutAHelpProvider() {
        assertViolates(HelpRuleViolationException.INVALID_HELP_PROVIDER, () -> raised(burningCatastrophe())
                .answer(HelpId.newId(), HelpProviderType.GRANDMA_AVATAR, COOK, "Pretending", stayCalm(), NOW));
        assertViolates(HelpRuleViolationException.INVALID_HELP_PROVIDER, () -> raised(burningCatastrophe())
                .answer(HelpId.newId(), HelpProviderType.COMMUNITY, null, "Anonymous", stayCalm(), NOW));
    }

    @Test
    void answersFitThePublishedLanguage() {
        assertViolates(HelpRuleViolationException.INVALID_ANSWER,
                () -> new Substitute(BUTTERMILK, "Yoghurt", BigDecimal.ZERO, Unit.GRAM));
        assertViolates(HelpRuleViolationException.INVALID_ANSWER,
                () -> new Substitute(BUTTERMILK, "x".repeat(101), BigDecimal.ONE, Unit.GRAM));
        assertViolates(HelpRuleViolationException.INVALID_ANSWER, () -> new IngredientSubstitutes(SCONES, List.of()));
        assertViolates(HelpRuleViolationException.INVALID_ANSWER,
                () -> new CatastropheMitigation(Optional.empty(), "x".repeat(2001)));
        assertViolates(HelpRuleViolationException.INVALID_ANSWER,
                () -> new PreparationStepExplanation(SCONES, null, "Use a cold pan", List.of()));
        assertViolates(HelpRuleViolationException.INVALID_ANSWER,
                () -> new MenuProposal("Note", 0, Meal.DINNER, Optional.empty(), List.of(new Course(1, SOUP))));
        assertViolates(HelpRuleViolationException.INVALID_ANSWER,
                () -> new MenuProposal("Note", 6, Meal.DINNER, Optional.empty(), List.of()));
        assertViolates(HelpRuleViolationException.INVALID_ANSWER,
                () -> new MenuProposal("Note", 6, Meal.DINNER, Optional.empty(), Collections.nCopies(11, new Course(1, SOUP))));
        assertViolates(HelpRuleViolationException.INVALID_ANSWER, () -> new Course(0, SOUP));
        assertViolates(HelpRuleViolationException.INVALID_ANSWER, () -> raised(burningCatastrophe())
                .answer(HelpId.newId(), HelpProviderType.COMMUNITY, OTHER_COOK, "x".repeat(201), stayCalm(), NOW));
    }

    @Test
    void theProviderRevisesAHelpThatStillFitsTheRequest() {
        HelpRequest request = raised(burningCatastrophe());
        Help help = community(request, stayCalm());

        help.revise("Really, stay calm", new CatastropheMitigation(Optional.empty(), "Breathe"), request, NOW.plusSeconds(5));

        assertThat(help.answerTitle()).isEqualTo("Really, stay calm");
        assertThat(help.answer()).isEqualTo(new CatastropheMitigation(Optional.empty(), "Breathe"));
        assertThat(help.updatedAt()).isEqualTo(NOW.plusSeconds(5));
        assertViolates(HelpRuleViolationException.ANSWER_TYPE_MISMATCH, () -> help.revise(null, useAColdPan(), request, NOW));
        assertThat(help.isProvidedBy(OTHER_COOK)).isTrue();
        assertThat(help.isProvidedBy(COOK)).isFalse();
    }

    @Test
    void aChefsHelpIsVisibleOnlyToTheRequester() {
        Help chef = raised(dinnerForTheInLaws())
                .answer(HelpId.newId(), HelpProviderType.CHEF, OTHER_COOK, "Chef says", threeCourses(), NOW);
        Help community = community(raised(burningCatastrophe()), stayCalm());

        assertThat(chef.isVisibleTo(COOK)).isTrue();
        assertThat(chef.isVisibleTo(OTHER_COOK)).isFalse();
        assertThat(community.isVisibleTo(new CookId(UUID.randomUUID()))).isTrue();
    }
}
