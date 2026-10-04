package org.larder.cookingassistance.domain;

/**
 * The content of a help - exactly one of four kinds. Its {@link #type()} must be the type of the
 * answered help request, and the recipe, step and ingredients it refers to must be those of the
 * request (checked by {@link HelpRequest#verifyAnswer(Answer)}).
 */
public sealed interface Answer permits IngredientSubstitutes, PreparationStepExplanation, CatastropheMitigation, MenuProposal {

    HelpType type();
}
