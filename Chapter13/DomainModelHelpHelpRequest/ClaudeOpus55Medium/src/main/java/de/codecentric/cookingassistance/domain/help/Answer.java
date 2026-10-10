package de.codecentric.cookingassistance.domain.help;

import de.codecentric.cookingassistance.domain.shared.AnswerType;

/**
 * Glossary term "answer" — exactly one of the four "oneOf" branches.
 *
 * <p>Each branch on the glossary "contains 1 answerType" fixed to one value,
 * so the answer type is a property of the branch, not a free field: an
 * answer can never carry a type that contradicts its content.
 */
public sealed interface Answer
        permits Substitutes, PreparationStepExplanation, CatastropheMitigation, MenuProposal {

    AnswerType answerType();
}
