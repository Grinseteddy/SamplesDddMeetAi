package org.larder.cookingassistance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.cookingassistance.TestData.BUTTER;
import static org.larder.cookingassistance.TestData.BUTTERMILK;
import static org.larder.cookingassistance.TestData.COOK;
import static org.larder.cookingassistance.TestData.FOLD_IN;
import static org.larder.cookingassistance.TestData.NOW;
import static org.larder.cookingassistance.TestData.SCONES;
import static org.larder.cookingassistance.TestData.SOUP;
import static org.larder.cookingassistance.TestData.burningCatastrophe;
import static org.larder.cookingassistance.TestData.dinnerForTheInLaws;
import static org.larder.cookingassistance.TestData.foldingTheDough;
import static org.larder.cookingassistance.TestData.noButtermilk;
import static org.larder.cookingassistance.TestData.providers;
import static org.larder.cookingassistance.TestData.stayCalm;

import java.util.Set;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

class HelpRequestTest {

    private static void assertViolates(String code, ThrowingCallable action) {
        assertThatThrownBy(action).isInstanceOf(HelpRuleViolationException.class)
                .satisfies(e -> assertThat(((HelpRuleViolationException) e).code()).isEqualTo(code));
    }

    private static HelpRequestDraft with(HelpRequestDraft draft, RecipeId recipe, HowToStepId step, Set<IngredientId> ingredients) {
        return new HelpRequestDraft(draft.title(), draft.type(), draft.description(), recipe, step, ingredients,
                draft.preferredProviders());
    }

    private static HelpRequestDraft withProviders(HelpRequestDraft draft, Set<HelpProviderType> providers) {
        return new HelpRequestDraft(draft.title(), draft.type(), draft.description(), draft.recipe(), draft.howToStep(),
                draft.ingredients(), providers);
    }

    @Test
    void aNewRequestIsOpenAndBelongsToItsRequester() {
        HelpRequest request = HelpRequest.raise(COOK, burningCatastrophe(), NOW);

        assertThat(request.status()).isEqualTo(HelpRequestStatus.OPEN);
        assertThat(request.isRaisedBy(COOK)).isTrue();
        assertThat(request.recipe()).contains(SCONES);
        assertThat(request.preferredProviders()).containsExactly(HelpProviderType.GRANDMA_AVATAR, HelpProviderType.COMMUNITY);
        assertThat(request.createdAt()).isEqualTo(NOW);
    }

    @Test
    void everyTypeOfTheGlossaryExampleIsValid() {
        assertThatCode(() -> HelpRequest.raise(COOK, foldingTheDough(), NOW)).doesNotThrowAnyException();
        assertThatCode(() -> HelpRequest.raise(COOK, noButtermilk(), NOW)).doesNotThrowAnyException();
        assertThatCode(() -> HelpRequest.raise(COOK, dinnerForTheInLaws(), NOW)).doesNotThrowAnyException();
        assertThatCode(() -> HelpRequest.raise(COOK, with(burningCatastrophe(), null, null, Set.of()), NOW))
                .doesNotThrowAnyException();
    }

    @Test
    void aRecipeIsMandatoryForStepExplanationsAndSubstitutes() {
        assertViolates(HelpRuleViolationException.RECIPE_REQUIRED,
                () -> HelpRequest.raise(COOK, with(foldingTheDough(), null, FOLD_IN, Set.of()), NOW));
        assertViolates(HelpRuleViolationException.RECIPE_REQUIRED,
                () -> HelpRequest.raise(COOK, with(noButtermilk(), null, null, Set.of(BUTTERMILK)), NOW));
    }

    @Test
    void aHowToStepIsRequiredForAndOnlyAllowedForStepExplanations() {
        assertViolates(HelpRuleViolationException.HOW_TO_STEP_REQUIRED,
                () -> HelpRequest.raise(COOK, with(foldingTheDough(), SCONES, null, Set.of()), NOW));
        assertViolates(HelpRuleViolationException.HOW_TO_STEP_NOT_ALLOWED,
                () -> HelpRequest.raise(COOK, with(burningCatastrophe(), SCONES, FOLD_IN, Set.of()), NOW));
        assertViolates(HelpRuleViolationException.HOW_TO_STEP_NOT_ALLOWED,
                () -> HelpRequest.raise(COOK, with(noButtermilk(), SCONES, FOLD_IN, Set.of(BUTTERMILK)), NOW));
    }

    @Test
    void ingredientsAreRequiredForAndOnlyAllowedForSubstitutes() {
        assertViolates(HelpRuleViolationException.INGREDIENTS_REQUIRED,
                () -> HelpRequest.raise(COOK, with(noButtermilk(), SCONES, null, Set.of()), NOW));
        assertViolates(HelpRuleViolationException.INGREDIENTS_NOT_ALLOWED,
                () -> HelpRequest.raise(COOK, with(burningCatastrophe(), SCONES, null, Set.of(BUTTERMILK)), NOW));
        assertViolates(HelpRuleViolationException.INGREDIENTS_NOT_ALLOWED,
                () -> HelpRequest.raise(COOK, with(foldingTheDough(), SCONES, FOLD_IN, Set.of(BUTTERMILK)), NOW));
    }

    @Test
    void chefSupportIsOnlyForMenuProposalsAndExclusive() {
        assertViolates(HelpRuleViolationException.CHEF_ONLY_FOR_MENU_PROPOSAL,
                () -> HelpRequest.raise(COOK, withProviders(burningCatastrophe(), providers(HelpProviderType.CHEF)), NOW));
        assertViolates(HelpRuleViolationException.CHEF_EXCLUSIVE, () -> HelpRequest.raise(COOK,
                withProviders(dinnerForTheInLaws(), providers(HelpProviderType.CHEF, HelpProviderType.COMMUNITY)), NOW));
        assertThatCode(() -> HelpRequest.raise(COOK, withProviders(dinnerForTheInLaws(),
                providers(HelpProviderType.GRANDMA_AVATAR, HelpProviderType.COMMUNITY)), NOW)).doesNotThrowAnyException();
    }

    @Test
    void oneOrTwoPreferredProviders() {
        assertViolates(HelpRuleViolationException.INVALID_HELP_REQUEST,
                () -> HelpRequest.raise(COOK, withProviders(burningCatastrophe(), Set.of()), NOW));
        assertViolates(HelpRuleViolationException.INVALID_HELP_REQUEST, () -> HelpRequest.raise(COOK,
                withProviders(dinnerForTheInLaws(), providers(HelpProviderType.GRANDMA_AVATAR, HelpProviderType.COMMUNITY,
                        HelpProviderType.CHEF)), NOW));
    }

    @Test
    void titleAndDescriptionFitThePublishedLanguage() {
        HelpRequestDraft draft = burningCatastrophe();
        assertViolates(HelpRuleViolationException.INVALID_HELP_REQUEST, () -> HelpRequest.raise(COOK,
                new HelpRequestDraft(" ", draft.type(), draft.description(), SCONES, null, Set.of(),
                        draft.preferredProviders()), NOW));
        assertViolates(HelpRuleViolationException.INVALID_HELP_REQUEST, () -> HelpRequest.raise(COOK,
                new HelpRequestDraft(draft.title(), draft.type(), "x".repeat(2001), SCONES, null, Set.of(),
                        draft.preferredProviders()), NOW));
        assertViolates(HelpRuleViolationException.INVALID_HELP_REQUEST, () -> HelpRequest.raise(COOK,
                new HelpRequestDraft(draft.title(), null, draft.description(), SCONES, null, Set.of(),
                        draft.preferredProviders()), NOW));
    }

    @Test
    void anOpenRequestIsRevisedAndTheRulesAreRecheckedOnTheResult() {
        HelpRequest request = HelpRequest.raise(COOK, noButtermilk(), NOW);

        request.revise(HelpRequestRevision.none().withTitle("No buttermilk, no butter")
                .withIngredients(Set.of(BUTTERMILK, BUTTER)), false, NOW.plusSeconds(60));

        assertThat(request.title()).isEqualTo("No buttermilk, no butter");
        assertThat(request.ingredients()).containsExactlyInAnyOrder(BUTTERMILK, BUTTER);
        assertThat(request.updatedAt()).isEqualTo(NOW.plusSeconds(60));
        assertViolates(HelpRuleViolationException.HOW_TO_STEP_NOT_ALLOWED,
                () -> request.revise(HelpRequestRevision.none().withHowToStep(FOLD_IN), false, NOW));
        assertViolates(HelpRuleViolationException.CHEF_ONLY_FOR_MENU_PROPOSAL, () -> request.revise(
                HelpRequestRevision.none().withPreferredProviders(providers(HelpProviderType.CHEF)), false, NOW));
        assertThat(request.howToStep()).isEmpty();
    }

    @Test
    void statusAnsweredNeedsAHelp() {
        HelpRequest request = HelpRequest.raise(COOK, burningCatastrophe(), NOW);

        assertViolates(HelpRuleViolationException.ANSWERED_WITHOUT_HELP,
                () -> request.revise(HelpRequestRevision.none().withStatus(HelpRequestStatus.ANSWERED), false, NOW));
        assertThat(request.status()).isEqualTo(HelpRequestStatus.OPEN);

        request.revise(HelpRequestRevision.none().withStatus(HelpRequestStatus.ANSWERED), true, NOW);
        assertThat(request.status()).isEqualTo(HelpRequestStatus.ANSWERED);
    }

    @Test
    void anAnsweredRequestKeepsWhatItsHelpsReferTo() {
        HelpRequest request = HelpRequest.raise(COOK, burningCatastrophe(), NOW);
        request.answer(HelpId.newId(), HelpProviderType.COMMUNITY, COOK, "Stay calm", stayCalm(), NOW);

        assertViolates(HelpRuleViolationException.HELP_REQUEST_ANSWERED,
                () -> request.revise(HelpRequestRevision.none().withStatus(HelpRequestStatus.OPEN), true, NOW));
        assertViolates(HelpRuleViolationException.HELP_REQUEST_ANSWERED,
                () -> request.revise(HelpRequestRevision.none().withRecipe(SOUP), true, NOW));
        assertViolates(HelpRuleViolationException.HELP_REQUEST_ANSWERED, () -> request.revise(
                HelpRequestRevision.none().withPreferredProviders(providers(HelpProviderType.COMMUNITY)), true, NOW));

        request.revise(HelpRequestRevision.none().withTitle("Burnt scones").withRecipe(SCONES), true, NOW);
        assertThat(request.title()).isEqualTo("Burnt scones");
        assertThat(request.status()).isEqualTo(HelpRequestStatus.ANSWERED);
    }

    @Test
    void onlyAnOpenRequestCanBeWithdrawn() {
        HelpRequest request = HelpRequest.raise(COOK, burningCatastrophe(), NOW);
        assertThatCode(request::checkCanBeWithdrawn).doesNotThrowAnyException();

        request.answer(HelpId.newId(), HelpProviderType.GRANDMA_AVATAR, null, "Stay calm", stayCalm(), NOW);

        assertViolates(HelpRuleViolationException.HELP_REQUEST_NOT_OPEN, request::checkCanBeWithdrawn);
    }

    @Test
    void withdrawingTheLastHelpOpensTheRequestAgain() {
        HelpRequest request = HelpRequest.raise(COOK, burningCatastrophe(), NOW);
        request.answer(HelpId.newId(), HelpProviderType.GRANDMA_AVATAR, null, "Stay calm", stayCalm(), NOW);

        request.helpWithdrawn(true, NOW);
        assertThat(request.status()).isEqualTo(HelpRequestStatus.ANSWERED);

        request.helpWithdrawn(false, NOW);
        assertThat(request.status()).isEqualTo(HelpRequestStatus.OPEN);
    }
}
