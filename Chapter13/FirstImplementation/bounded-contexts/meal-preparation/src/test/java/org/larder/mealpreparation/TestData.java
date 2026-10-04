package org.larder.mealpreparation;

import java.util.UUID;

import org.larder.mealpreparation.domain.CookId;
import org.larder.mealpreparation.domain.HowToStep;
import org.larder.mealpreparation.domain.HowToStepId;
import org.larder.mealpreparation.domain.RecipeId;
import org.larder.mealpreparation.domain.RecipeSteps;

/** The examples of the contract and the visual glossary. */
public final class TestData {

    public static final CookId COOK = new CookId(UUID.fromString("f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074"));
    public static final CookId OTHER_COOK = new CookId(UUID.fromString("39a7aed5-2e50-48c8-8aa6-f3afa9f03f74"));
    public static final RecipeId SCONES = new RecipeId(UUID.fromString("7cf09822-77a1-46bb-812f-b7852bca0913"));

    /** "Carefully mix the water with the flour" - the contract's example step. */
    public static final HowToStep MIX = step("65610dee-fb83-4341-a730-26a4a99a621a", 1);
    public static final HowToStep KNEAD = step("0b4c7f3e-2d6a-4e8b-9f1c-3a5d7e9b1c2d", 2);
    public static final HowToStep BAKE = step("9e8d7c6b-5a49-4382-a1b0-c9d8e7f6a5b4", 3);

    /** The steps of the Scones recipe in the recipe's order. */
    public static RecipeSteps sconeSteps() {
        return RecipeSteps.of(MIX, KNEAD, BAKE);
    }

    private static HowToStep step(String id, int sequenceNumber) {
        return new HowToStep(new HowToStepId(UUID.fromString(id)), sequenceNumber);
    }

    private TestData() {
    }
}
