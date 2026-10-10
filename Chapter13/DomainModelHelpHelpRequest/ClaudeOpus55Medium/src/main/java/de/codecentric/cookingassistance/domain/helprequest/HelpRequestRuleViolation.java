package de.codecentric.cookingassistance.domain.helprequest;

import de.codecentric.cookingassistance.domain.shared.AnswerType;
import de.codecentric.cookingassistance.domain.shared.DomainRuleViolation;
import de.codecentric.cookingassistance.domain.shared.HelpRequestId;

/** Rules of the Help Request glossary, one exception per purple invariant sticky or cardinality. */
public abstract class HelpRequestRuleViolation extends DomainRuleViolation {

    protected HelpRequestRuleViolation(String message) {
        super(message);
    }

    /** Invariant sticky on "recipe": "Mandatory" for Preparation Step Explanation and Ingredient Substitute. */
    public static final class RecipeMandatoryForType extends HelpRequestRuleViolation {
        public RecipeMandatoryForType(AnswerType type) {
            super("a recipe is mandatory for help of type " + type);
        }
    }

    /** Invariant sticky on "howTo Step": "Only for type" Preparation Step Explanation. */
    public static final class HowToStepOnlyForPreparationStepExplanation extends HelpRequestRuleViolation {
        public HowToStepOnlyForPreparationStepExplanation(AnswerType type) {
            super("a howTo step only belongs to a Preparation Step Explanation request, not " + type);
        }
    }

    /** Invariant sticky on "ingredients": "Only for type" Ingredient Substitute. */
    public static final class IngredientsOnlyForIngredientSubstitute extends HelpRequestRuleViolation {
        public IngredientsOnlyForIngredientSubstitute(AnswerType type) {
            super("ingredients only belong to an Ingredient Substitute request, not " + type);
        }
    }

    /** Cardinality 1..2 on "preferred Provider". */
    public static final class PreferredProviderCountOutOfRange extends HelpRequestRuleViolation {
        public PreferredProviderCountOutOfRange(int count) {
            super("a help request names " + HelpRequest.MIN_PREFERRED_PROVIDERS + " to "
                    + HelpRequest.MAX_PREFERRED_PROVIDERS + " preferred providers, not " + count);
        }
    }

    /** "preferred Provider" values are a set of the three provider types — naming one twice says nothing. */
    public static final class DuplicatePreferredProvider extends HelpRequestRuleViolation {
        public DuplicatePreferredProvider() {
            super("each preferred provider may be named only once");
        }
    }

    /** Invariant sticky on "preferred Provider": "Chef help is provided exclusively". */
    public static final class ChefHelpMustBeExclusive extends HelpRequestRuleViolation {
        public ChefHelpMustBeExclusive() {
            super("Chef help is provided exclusively — do not combine Chef with another provider");
        }
    }

    /** Invariant stickies "Chef support only for menu proposal" / "Only for type" Menu proposal. */
    public static final class ChefSupportOnlyForMenuProposal extends HelpRequestRuleViolation {
        public ChefSupportOnlyForMenuProposal(AnswerType type) {
            super("Chef support is only available for a Menu proposal, not " + type);
        }
    }

    /** A Help Request in status Closed accepts nothing more. */
    public static final class HelpRequestClosed extends HelpRequestRuleViolation {
        public HelpRequestClosed(HelpRequestId id) {
            super("help request " + id.value() + " is closed");
        }
    }

    /** An accepted Help was reported to a Help Request it does not refer to. */
    public static final class AcceptedHelpForOtherRequest extends HelpRequestRuleViolation {
        public AcceptedHelpForOtherRequest(HelpRequestId expected, HelpRequestId actual) {
            super("accepted help refers to request " + actual.value() + ", not " + expected.value());
        }
    }
}
