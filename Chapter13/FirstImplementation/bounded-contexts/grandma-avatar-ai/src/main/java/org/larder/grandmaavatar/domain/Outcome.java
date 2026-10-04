package org.larder.grandmaavatar.domain;

/** What Grandma did with a help request. */
public enum Outcome {
    /** She answered it; a Help was published. */
    ANSWERED,
    /** Not her business: the request is not open or the cook did not name her (e.g. chef-only). */
    NOT_FOR_GRANDMA,
    /** She had no advice for it (e.g. her recipe box has no answer of that kind). */
    NO_ADVICE,
    /** Her advisor came up with an answer that breaks the invariants of a Help; it was discarded. */
    ADVICE_REJECTED
}
