package de.codecentric.cookingassistance.domain.shared;

/**
 * The kind of help — glossary term "answerType" on Help and "type" on Help
 * Request. Both stickies list the same four values, and the Help glossary
 * states "Must be the same as request type", so it is one type shared by both
 * aggregates.
 */
public enum AnswerType {
    INGREDIENT_SUBSTITUTE,
    PREPARATION_STEP_EXPLANATION,
    STEPS_TO_MITIGATE_CATASTROPHE,
    MENU_PROPOSAL
}
