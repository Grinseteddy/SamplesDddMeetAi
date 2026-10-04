package org.larder.mealpreparation.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.larder.mealpreparation.TestData.BAKE;
import static org.larder.mealpreparation.TestData.COOK;
import static org.larder.mealpreparation.TestData.KNEAD;
import static org.larder.mealpreparation.TestData.MIX;
import static org.larder.mealpreparation.TestData.OTHER_COOK;
import static org.larder.mealpreparation.TestData.SCONES;
import static org.larder.mealpreparation.TestData.sconeSteps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.mealpreparation.domain.MealPreparation;
import org.larder.mealpreparation.domain.PreparationId;
import org.larder.platform.test.TestDatabase;

/** Runs the real migrations against PostgreSQL in its own schema with its own user. */
class JdbcMealPreparationRepositoryTest {

    private JdbcMealPreparationRepository preparations;

    @BeforeEach
    void freshSchema() {
        preparations = new JdbcMealPreparationRepository(TestDatabase.forSchema("mealpreparation").jdbcClient());
    }

    @Test
    void storesAPreparationWithItsSnapshotOfSteps() {
        MealPreparation preparation = MealPreparation.start(COOK, SCONES, sconeSteps());
        preparations.save(preparation);

        MealPreparation stored = preparations.findById(preparation.id()).orElseThrow();

        assertThat(stored.cook()).isEqualTo(COOK);
        assertThat(stored.recipe()).isEqualTo(SCONES);
        assertThat(stored.steps().steps()).containsExactly(MIX, KNEAD, BAKE);
        assertThat(stored.currentStep()).isEqualTo(MIX);
    }

    @Test
    void storesTheMovedCurrentStepAndKeepsTheSnapshot() {
        MealPreparation preparation = MealPreparation.start(COOK, SCONES, sconeSteps());
        preparations.save(preparation);

        MealPreparation loaded = preparations.findByIdForMove(preparation.id()).orElseThrow();
        loaded.moveToNext(MIX.id());
        loaded.moveToNext(KNEAD.id());
        preparations.save(loaded);

        MealPreparation stored = preparations.findById(preparation.id()).orElseThrow();
        assertThat(stored.currentStep()).isEqualTo(BAKE);
        assertThat(stored.steps().steps()).containsExactly(MIX, KNEAD, BAKE);
    }

    @Test
    void keepsPreparationsOfTheSameRecipeApart() {
        MealPreparation mine = MealPreparation.start(COOK, SCONES, sconeSteps());
        MealPreparation theirs = MealPreparation.start(OTHER_COOK, SCONES, sconeSteps());
        preparations.save(mine);
        preparations.save(theirs);
        theirs.moveToNext(MIX.id());
        preparations.save(theirs);

        assertThat(preparations.findById(mine.id()).orElseThrow().currentStep()).isEqualTo(MIX);
        assertThat(preparations.findById(theirs.id()).orElseThrow().currentStep()).isEqualTo(KNEAD);
        assertThat(preparations.findById(theirs.id()).orElseThrow().isStartedBy(OTHER_COOK)).isTrue();
    }

    @Test
    void anUnknownPreparationIsNotThere() {
        assertThat(preparations.findById(PreparationId.newId())).isEmpty();
        assertThat(preparations.findByIdForMove(PreparationId.newId())).isEmpty();
    }
}
