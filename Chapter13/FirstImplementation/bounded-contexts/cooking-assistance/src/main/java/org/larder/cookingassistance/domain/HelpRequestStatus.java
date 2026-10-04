package org.larder.cookingassistance.domain;

/** A help request starts Open and is Answered as soon as - and as long as - at least one help answers it. */
public enum HelpRequestStatus {
    OPEN,
    ANSWERED
}
