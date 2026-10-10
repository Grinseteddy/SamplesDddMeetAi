package de.codecentric.cookingassistance.domain.help;

import de.codecentric.cookingassistance.domain.external.IngredientId;
import de.codecentric.cookingassistance.domain.shared.AnswerType;
import de.codecentric.cookingassistance.domain.shared.DomainRuleViolation;
import de.codecentric.cookingassistance.domain.shared.HelpId;
import de.codecentric.cookingassistance.domain.shared.HelpRequestId;

/** Rules of the Help glossary, one exception per purple invariant sticky. */
public abstract class HelpRuleViolation extends DomainRuleViolation {

    protected HelpRuleViolation(String message) {
        super(message);
    }

    /** "Must be the same as request type". */
    public static final class AnswerTypeNotAsRequested extends HelpRuleViolation {
        public AnswerTypeNotAsRequested(AnswerType requested, AnswerType answered) {
            super("the request asks for " + requested + ", the answer is " + answered);
        }
    }

    /** "Same recipe as in request". */
    public static final class RecipeNotAsInRequest extends HelpRuleViolation {
        public RecipeNotAsInRequest() {
            super("the answer must refer to the same recipe as the request");
        }
    }

    /** "Same howTo as in request". */
    public static final class HowToStepNotAsInRequest extends HelpRuleViolation {
        public HowToStepNotAsInRequest() {
            super("the answer must explain the same howTo step the request asks about");
        }
    }

    /** "same ingredients as in request". */
    public static final class IngredientNotInRequest extends HelpRuleViolation {
        public IngredientNotInRequest(IngredientId ingredient) {
            super("ingredient " + ingredient.value() + " was not asked about in the request");
        }
    }

    /** "A cook can answer their own request - except for Chef requests". */
    public static final class CannotAnswerOwnChefRequest extends HelpRuleViolation {
        public CannotAnswerOwnChefRequest() {
            super("a cook cannot answer their own Chef request");
        }
    }

    /** Help is only provided or accepted while its request is Open or Answered. */
    public static final class HelpRequestClosed extends HelpRuleViolation {
        public HelpRequestClosed(HelpRequestId id) {
            super("help request " + id.value() + " is closed");
        }
    }

    /** "refers to 1 help request" — the request handed in is not that one. */
    public static final class HelpBelongsToOtherRequest extends HelpRuleViolation {
        public HelpBelongsToOtherRequest(HelpId help, HelpRequestId request) {
            super("help " + help.value() + " does not refer to request " + request.value());
        }
    }

    /** "isAccepted" — marked "!" on the glossary; accepting twice is refused. */
    public static final class HelpAlreadyAccepted extends HelpRuleViolation {
        public HelpAlreadyAccepted(HelpId help) {
            super("help " + help.value() + " has already been accepted");
        }
    }
}
