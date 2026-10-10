package cookingassistance.domain;

import static cookingassistance.domain.DomainViolation.*;
/** Application must load canonical aggregates and persist BOTH atomically with concurrency checks. */
public final class CookingAssistance {
    public void accept(HelpRequest request, Help help) {
        required(request,"HelpRequest"); required(help,"Help");
        help.validateAgainst(request); // Validate all failure conditions before changing either aggregate.
        help.accept(); request.answeredBy(help);
    }
}
