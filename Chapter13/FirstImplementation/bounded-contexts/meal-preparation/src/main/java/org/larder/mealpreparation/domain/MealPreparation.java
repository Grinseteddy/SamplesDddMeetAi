package org.larder.mealpreparation.domain;

import static org.larder.mealpreparation.domain.MealPreparationRuleViolationException.FIRST_STEP_REACHED;
import static org.larder.mealpreparation.domain.MealPreparationRuleViolationException.LAST_STEP_REACHED;
import static org.larder.mealpreparation.domain.MealPreparationRuleViolationException.RECIPE_WITHOUT_STEPS;
import static org.larder.mealpreparation.domain.MealPreparationRuleViolationException.STEP_NOT_CURRENT;

import java.util.Objects;
import java.util.Optional;

/**
 * Carries one cook through one recipe in real time (aggregate root). Refers to exactly one recipe
 * and has exactly one current step.
 *
 * <ul>
 *   <li><b>Snapshot.</b> The recipe's how-to steps are copied when the preparation starts and never
 *       change afterwards. If the recipe's owner edits, adds, reorders or removes steps in Recipe
 *       Catalog while the cook is at the stove, this preparation keeps the steps it started with -
 *       the cook is not thrown to another step mid-way.</li>
 *   <li>A preparation needs at least one step; it starts on the first step of the recipe's order
 *       (the lowest sequence number, 1 for a recipe without gaps).</li>
 *   <li>Moving names the step the cook moves away from. It must be the current step, so a repeated
 *       or out-of-date request does not move the preparation twice.</li>
 *   <li>Next on the last step and previous on the first step are refused; the current step stays.</li>
 *   <li>Reading a step never moves the current step.</li>
 * </ul>
 */
public final class MealPreparation {

    private final PreparationId id;
    private final CookId cook;
    private final RecipeId recipe;
    private final RecipeSteps steps;
    private int current;

    private MealPreparation(PreparationId id, CookId cook, RecipeId recipe, RecipeSteps steps, int current) {
        this.id = Objects.requireNonNull(id);
        this.cook = Objects.requireNonNull(cook);
        this.recipe = Objects.requireNonNull(recipe);
        this.steps = Objects.requireNonNull(steps);
        this.current = current;
    }

    /** {@code cook} starts preparing {@code recipe} with the recipe's steps as they are now. */
    public static MealPreparation start(CookId cook, RecipeId recipe, RecipeSteps steps) {
        Objects.requireNonNull(steps, "A meal preparation needs the recipe's steps");
        if (steps.isEmpty()) {
            throw new MealPreparationRuleViolationException(RECIPE_WITHOUT_STEPS,
                    "Recipe " + recipe.value() + " has no how-to steps to prepare");
        }
        return new MealPreparation(PreparationId.newId(), cook, recipe, steps, 0);
    }

    /** Recreates a stored meal preparation. */
    public static MealPreparation restore(PreparationId id, CookId cook, RecipeId recipe, RecipeSteps steps,
                                          HowToStepId currentStep) {
        int current = steps.positionOf(currentStep);
        if (current < 0) {
            throw new IllegalArgumentException("The current step " + currentStep.value() + " is not a step of preparation "
                    + id.value());
        }
        return new MealPreparation(id, cook, recipe, steps, current);
    }

    /** Moves on from {@code from} to the next step and returns the step that is now current. */
    public HowToStep moveToNext(HowToStepId from) {
        requireCurrent(from);
        if (current == steps.size() - 1) {
            throw new MealPreparationRuleViolationException(LAST_STEP_REACHED,
                    "Step " + from.value() + " is the last how-to step; there is no next one");
        }
        current++;
        return currentStep();
    }

    /** Moves back from {@code from} to the previous step and returns the step that is now current. */
    public HowToStep moveToPrevious(HowToStepId from) {
        requireCurrent(from);
        if (current == 0) {
            throw new MealPreparationRuleViolationException(FIRST_STEP_REACHED,
                    "Step " + from.value() + " is the first how-to step; there is no previous one");
        }
        current--;
        return currentStep();
    }

    private void requireCurrent(HowToStepId from) {
        if (!currentStep().id().equals(from)) {
            throw new MealPreparationRuleViolationException(STEP_NOT_CURRENT,
                    "Step " + from.value() + " is not the current step; the current step is "
                            + currentStep().id().value());
        }
    }

    /** One step of this preparation's snapshot; reading it leaves the current step as it is. */
    public Optional<HowToStep> step(HowToStepId stepId) {
        return steps.find(stepId);
    }

    public boolean isStartedBy(CookId candidate) {
        return cook.equals(candidate);
    }

    public HowToStep currentStep() {
        return steps.at(current);
    }

    public PreparationId id() {
        return id;
    }

    public CookId cook() {
        return cook;
    }

    public RecipeId recipe() {
        return recipe;
    }

    public RecipeSteps steps() {
        return steps;
    }
}
