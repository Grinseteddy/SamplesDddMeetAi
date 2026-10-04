package org.larder.cookingassistance.application;

/** What receiving a help of the Grandma Avatar did. */
public enum GrandmaHelpOutcome {
    /** Stored as a new help; the request is Answered and HelpProvided is queued. */
    RECORDED,
    /** The same help was received before (redelivery) - nothing changed, nothing published. */
    DUPLICATE
}
