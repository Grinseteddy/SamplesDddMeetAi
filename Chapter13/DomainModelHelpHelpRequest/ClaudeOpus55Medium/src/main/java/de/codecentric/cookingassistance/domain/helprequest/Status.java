package de.codecentric.cookingassistance.domain.helprequest;

/**
 * Glossary term "status" of a Help Request.
 *
 * <ul>
 *   <li>{@link #OPEN} — raised, no Help accepted yet.</li>
 *   <li>{@link #ANSWERED} — "At least one Help must be accepted";
 *       "Answered help requests can be still answered".</li>
 *   <li>{@link #CLOSED} — marked "!" on the glossary: how and by whom a request
 *       is closed is an open question. Modelled as terminal.</li>
 * </ul>
 */
public enum Status {
    OPEN,
    ANSWERED,
    CLOSED
}
