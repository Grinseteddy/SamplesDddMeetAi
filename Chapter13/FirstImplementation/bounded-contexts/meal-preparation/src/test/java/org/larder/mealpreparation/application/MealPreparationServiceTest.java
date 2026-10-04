package org.larder.mealpreparation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.mealpreparation.TestData.BAKE;
import static org.larder.mealpreparation.TestData.COOK;
import static org.larder.mealpreparation.TestData.KNEAD;
import static org.larder.mealpreparation.TestData.MIX;
import static org.larder.mealpreparation.TestData.OTHER_COOK;
import static org.larder.mealpreparation.TestData.SCONES;
import static org.larder.mealpreparation.TestData.sconeSteps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.larder.mealpreparation.domain.HowToStep;
import org.larder.mealpreparation.domain.HowToStepId;
import org.larder.mealpreparation.domain.MealPreparation;
import org.larder.mealpreparation.domain.MealPreparationRuleViolationException;
import org.larder.mealpreparation.domain.PreparationId;
import org.larder.mealpreparation.domain.RecipeId;
import org.larder.mealpreparation.domain.RecipeSteps;

class MealPreparationServiceTest {

    private final InMemoryPreparations preparations = new InMemoryPreparations();
    private final FakeRecipeCatalog recipeCatalog = new FakeRecipeCatalog();
    private final MealPreparationService service = new MealPreparationService(preparations, recipeCatalog);

    @Test
    void startsAPreparationOnTheFirstStepOfAKnownRecipe() {
        recipeCatalog.recipes.put(SCONES, sconeSteps());

        MealPreparation preparation = service.start(COOK, SCONES);

        assertThat(preparations.findById(preparation.id())).isPresent();
        assertThat(service.preparation(COOK, preparation.id()).currentStep()).isEqualTo(MIX);
    }

    @Test
    void anUnknownRecipeCannotBePrepared() {
        assertThatThrownBy(() -> service.start(COOK, SCONES)).isInstanceOf(UnknownRecipeException.class);
        assertThat(preparations.store).isEmpty();
    }

    @Test
    void aRecipeWithoutStepsCannotBePrepared() {
        recipeCatalog.recipes.put(SCONES, new RecipeSteps(List.of()));

        assertThatThrownBy(() -> service.start(COOK, SCONES)).isInstanceOf(MealPreparationRuleViolationException.class);
        assertThat(preparations.store).isEmpty();
    }

    @Test
    void recipeChangesAfterTheStartDoNotReachThePreparation() {
        recipeCatalog.recipes.put(SCONES, sconeSteps());
        MealPreparation preparation = service.start(COOK, SCONES);

        recipeCatalog.recipes.put(SCONES, RecipeSteps.of(new HowToStep(new HowToStepId(UUID.randomUUID()), 1)));

        assertThat(service.moveToNext(COOK, preparation.id(), MIX.id())).isEqualTo(KNEAD);
        assertThat(service.howToStep(COOK, preparation.id(), BAKE.id())).isEqualTo(BAKE);
    }

    @Test
    void movesAndStoresTheCurrentStep() {
        PreparationId id = started();

        assertThat(service.moveToNext(COOK, id, MIX.id())).isEqualTo(KNEAD);
        assertThat(service.moveToNext(COOK, id, KNEAD.id())).isEqualTo(BAKE);
        assertThat(service.moveToPrevious(COOK, id, BAKE.id())).isEqualTo(KNEAD);
        assertThat(service.preparation(COOK, id).currentStep()).isEqualTo(KNEAD);
        assertThat(preparations.lockedReads).isEqualTo(3);
    }

    @Test
    void aStaleMoveIsRefusedAndChangesNothing() {
        PreparationId id = started();
        service.moveToNext(COOK, id, MIX.id());

        assertThatThrownBy(() -> service.moveToNext(COOK, id, MIX.id()))
                .isInstanceOf(MealPreparationRuleViolationException.class);
        assertThat(service.preparation(COOK, id).currentStep()).isEqualTo(KNEAD);
    }

    @Test
    void readingAStepLeavesTheCurrentStep() {
        PreparationId id = started();

        assertThat(service.howToStep(COOK, id, BAKE.id())).isEqualTo(BAKE);
        assertThat(service.preparation(COOK, id).currentStep()).isEqualTo(MIX);
        assertThatThrownBy(() -> service.howToStep(COOK, id, new HowToStepId(UUID.randomUUID())))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void onlyTheCookWhoStartedItUsesAPreparation() {
        PreparationId id = started();

        assertThatThrownBy(() -> service.preparation(OTHER_COOK, id)).isInstanceOf(NotPermittedException.class);
        assertThatThrownBy(() -> service.howToStep(OTHER_COOK, id, MIX.id())).isInstanceOf(NotPermittedException.class);
        assertThatThrownBy(() -> service.moveToNext(OTHER_COOK, id, MIX.id())).isInstanceOf(NotPermittedException.class);
        assertThatThrownBy(() -> service.moveToPrevious(OTHER_COOK, id, MIX.id())).isInstanceOf(NotPermittedException.class);
        assertThat(service.preparation(COOK, id).currentStep()).isEqualTo(MIX);
    }

    @Test
    void unknownPreparationsAreNotFound() {
        PreparationId unknown = PreparationId.newId();

        assertThatThrownBy(() -> service.preparation(COOK, unknown)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.moveToNext(COOK, unknown, MIX.id())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.howToStep(COOK, unknown, MIX.id())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void anUnavailableRecipeCatalogIsPassedOn() {
        recipeCatalog.failure = new RecipeCatalogUnavailableException("down", null);

        assertThatThrownBy(() -> service.start(COOK, SCONES)).isInstanceOf(RecipeCatalogUnavailableException.class);
    }

    private PreparationId started() {
        recipeCatalog.recipes.put(SCONES, sconeSteps());
        return service.start(COOK, SCONES).id();
    }

    /** Stores restored copies, as a database would, so unsaved changes do not leak. */
    static class InMemoryPreparations implements MealPreparationRepository {
        final Map<PreparationId, MealPreparation> store = new HashMap<>();
        int lockedReads;

        @Override
        public void save(MealPreparation preparation) {
            store.put(preparation.id(), copy(preparation));
        }

        @Override
        public Optional<MealPreparation> findById(PreparationId id) {
            return Optional.ofNullable(store.get(id)).map(InMemoryPreparations::copy);
        }

        @Override
        public Optional<MealPreparation> findByIdForMove(PreparationId id) {
            lockedReads++;
            return findById(id);
        }

        private static MealPreparation copy(MealPreparation p) {
            return MealPreparation.restore(p.id(), p.cook(), p.recipe(), p.steps(), p.currentStep().id());
        }
    }

    static class FakeRecipeCatalog implements RecipeCatalog {
        final Map<RecipeId, RecipeSteps> recipes = new HashMap<>();
        RuntimeException failure;

        @Override
        public Optional<RecipeSteps> stepsOf(RecipeId recipe) {
            if (failure != null) {
                throw failure;
            }
            return Optional.ofNullable(recipes.get(recipe));
        }
    }
}
