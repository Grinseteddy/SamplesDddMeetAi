package org.larder.grandmaavatar.domain;

import java.net.URI;
import java.util.List;

/** Explains one preparation step - the very step of the very recipe the cook is stuck at. */
public record PreparationStepExplanation(RecipeId recipe, HowToStepId howToStep, String description, List<URI> images)
        implements Answer {

    public PreparationStepExplanation {
        Answer.require(recipe != null, "a step explanation needs the recipe");
        Answer.require(howToStep != null, "a step explanation needs the howToStep");
        Texts.answerText(description, "description", 2000);
        images = images == null ? List.of() : List.copyOf(images);
        Answer.require(images.stream().allMatch(URI::isAbsolute), "images must be absolute URIs");
    }

    @Override
    public HelpType type() {
        return HelpType.PREPARATION_STEP_EXPLANATION;
    }

    @Override
    public void mustFit(HelpRequest request) {
        Answer.require(request.recipe().equals(java.util.Optional.of(recipe)), "the explanation must be for the requested recipe");
        Answer.require(request.howToStep().equals(java.util.Optional.of(howToStep)), "the explanation must be for the requested step");
    }
}
