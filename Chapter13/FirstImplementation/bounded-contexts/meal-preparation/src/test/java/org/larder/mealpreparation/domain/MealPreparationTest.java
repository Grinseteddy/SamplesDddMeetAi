package org.larder.mealpreparation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.mealpreparation.TestData.BAKE;
import static org.larder.mealpreparation.TestData.COOK;
import static org.larder.mealpreparation.TestData.KNEAD;
import static org.larder.mealpreparation.TestData.MIX;
import static org.larder.mealpreparation.TestData.OTHER_COOK;
import static org.larder.mealpreparation.TestData.SCONES;
import static org.larder.mealpreparation.TestData.sconeSteps;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class MealPreparationTest {

    @Test
    void startsOnTheFirstStepForTheCookWhoStartedIt() {
        MealPreparation preparation = MealPreparation.start(COOK, SCONES, sconeSteps());

        assertThat(preparation.currentStep()).isEqualTo(MIX);
        assertThat(preparation.recipe()).isEqualTo(SCONES);
        assertThat(preparation.isStartedBy(COOK)).isTrue();
        assertThat(preparation.isStartedBy(OTHER_COOK)).isFalse();
    }

    @Test
    void startsOnTheLowestSequenceNumberWhateverOrderTheStepsArriveIn() {
        MealPreparation preparation = MealPreparation.start(COOK, SCONES, RecipeSteps.of(BAKE, MIX, KNEAD));

        assertThat(preparation.currentStep()).isEqualTo(MIX);
        assertThat(preparation.steps().steps()).containsExactly(MIX, KNEAD, BAKE);
    }

    @Test
    void aRecipeWithoutStepsCannotBePrepared() {
        assertThatThrownBy(() -> MealPreparation.start(COOK, SCONES, new RecipeSteps(List.of())))
                .isInstanceOf(MealPreparationRuleViolationException.class)
                .extracting("code").isEqualTo(MealPreparationRuleViolationException.RECIPE_WITHOUT_STEPS);
    }

    @Test
    void movesForwardAndBackOneStepAtATime() {
        MealPreparation preparation = MealPreparation.start(COOK, SCONES, sconeSteps());

        assertThat(preparation.moveToNext(MIX.id())).isEqualTo(KNEAD);
        assertThat(preparation.moveToNext(KNEAD.id())).isEqualTo(BAKE);
        assertThat(preparation.moveToPrevious(BAKE.id())).isEqualTo(KNEAD);
        assertThat(preparation.currentStep()).isEqualTo(KNEAD);
    }

    @Test
    void nextOnTheLastStepIsRefusedAndLeavesTheCurrentStep() {
        MealPreparation preparation = MealPreparation.start(COOK, SCONES, sconeSteps());
        preparation.moveToNext(MIX.id());
        preparation.moveToNext(KNEAD.id());

        assertThatThrownBy(() -> preparation.moveToNext(BAKE.id()))
                .isInstanceOf(MealPreparationRuleViolationException.class)
                .extracting("code").isEqualTo(MealPreparationRuleViolationException.LAST_STEP_REACHED);
        assertThat(preparation.currentStep()).isEqualTo(BAKE);
    }

    @Test
    void previousOnTheFirstStepIsRefusedAndLeavesTheCurrentStep() {
        MealPreparation preparation = MealPreparation.start(COOK, SCONES, sconeSteps());

        assertThatThrownBy(() -> preparation.moveToPrevious(MIX.id()))
                .isInstanceOf(MealPreparationRuleViolationException.class)
                .extracting("code").isEqualTo(MealPreparationRuleViolationException.FIRST_STEP_REACHED);
        assertThat(preparation.currentStep()).isEqualTo(MIX);
    }

    @Test
    void aSingleStepRecipeCanMoveNeitherWay() {
        MealPreparation preparation = MealPreparation.start(COOK, SCONES, RecipeSteps.of(MIX));

        assertThatThrownBy(() -> preparation.moveToNext(MIX.id())).isInstanceOf(MealPreparationRuleViolationException.class);
        assertThatThrownBy(() -> preparation.moveToPrevious(MIX.id())).isInstanceOf(MealPreparationRuleViolationException.class);
        assertThat(preparation.currentStep()).isEqualTo(MIX);
    }

    @Test
    void aRepeatedMoveFromAStaleStepIsRefused() {
        MealPreparation preparation = MealPreparation.start(COOK, SCONES, sconeSteps());
        preparation.moveToNext(MIX.id());

        assertThatThrownBy(() -> preparation.moveToNext(MIX.id()))
                .isInstanceOf(MealPreparationRuleViolationException.class)
                .extracting("code").isEqualTo(MealPreparationRuleViolationException.STEP_NOT_CURRENT);
        assertThatThrownBy(() -> preparation.moveToPrevious(new HowToStepId(UUID.randomUUID())))
                .isInstanceOf(MealPreparationRuleViolationException.class)
                .extracting("code").isEqualTo(MealPreparationRuleViolationException.STEP_NOT_CURRENT);
        assertThat(preparation.currentStep()).isEqualTo(KNEAD);
    }

    @Test
    void readingAStepDoesNotMoveTheCurrentStep() {
        MealPreparation preparation = MealPreparation.start(COOK, SCONES, sconeSteps());

        assertThat(preparation.step(BAKE.id())).contains(BAKE);
        assertThat(preparation.step(new HowToStepId(UUID.randomUUID()))).isEmpty();
        assertThat(preparation.currentStep()).isEqualTo(MIX);
    }

    @Test
    void theStepsAreASnapshotThatLaterRecipeChangesDoNotReach() {
        List<HowToStep> recipeAsDelivered = new ArrayList<>(List.of(MIX, KNEAD, BAKE));
        MealPreparation preparation = MealPreparation.start(COOK, SCONES, new RecipeSteps(recipeAsDelivered));

        recipeAsDelivered.remove(KNEAD);
        recipeAsDelivered.add(new HowToStep(new HowToStepId(UUID.randomUUID()), 4));

        assertThat(preparation.steps().steps()).containsExactly(MIX, KNEAD, BAKE);
        assertThat(preparation.moveToNext(MIX.id())).isEqualTo(KNEAD);
    }

    @Test
    void restoresOnTheStoredCurrentStep() {
        MealPreparation started = MealPreparation.start(COOK, SCONES, sconeSteps());

        MealPreparation restored = MealPreparation.restore(started.id(), COOK, SCONES, sconeSteps(), KNEAD.id());

        assertThat(restored.currentStep()).isEqualTo(KNEAD);
        assertThatThrownBy(() -> MealPreparation.restore(started.id(), COOK, SCONES, sconeSteps(),
                new HowToStepId(UUID.randomUUID()))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void stepsAreUniqueAndNumberedFromOne() {
        assertThatThrownBy(() -> RecipeSteps.of(MIX, MIX)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RecipeSteps.of(MIX, new HowToStep(new HowToStepId(UUID.randomUUID()), 1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new HowToStep(MIX.id(), 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
