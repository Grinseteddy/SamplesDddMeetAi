package org.larder.sharing.domain;

import java.util.List;

/**
 * A partial change of thanks. {@code null} means "leave as it is"; recipients, when given, replace
 * the whole list (an empty list addresses the thanks to nobody).
 */
public record ThanksRevision(List<Recipient> recipients, String thanksText, Picture picture) {

    public ThanksRevision {
        recipients = recipients == null ? null : List.copyOf(recipients);
    }

    public static ThanksRevision none() {
        return new ThanksRevision(null, null, null);
    }

    public boolean changesRecipients() {
        return recipients != null;
    }

    public boolean changesPicture() {
        return picture != null;
    }

    public boolean changesNothing() {
        return recipients == null && thanksText == null && picture == null;
    }
}
