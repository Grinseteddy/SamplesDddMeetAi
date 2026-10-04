package org.larder.sharing.domain;

import static org.larder.sharing.domain.ThanksRuleViolationException.DUPLICATE_RECIPIENT;
import static org.larder.sharing.domain.ThanksRuleViolationException.INVALID_THANKS_TEXT;

import java.time.Instant;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Thanks a cook gives for one help (aggregate root).
 *
 * <ul>
 *   <li>Giver and help are fixed once the thanks are given; text, picture and recipients can change.</li>
 *   <li>The text has 1 to 2000 characters and is not blank; a picture is always shared.</li>
 *   <li>Thanks may name nobody; each recipient type appears at most once (one help has one helper).</li>
 *   <li>{@code version} counts the stored changes; a change is stored only onto the version it was made on.</li>
 * </ul>
 */
public final class Thanks {

    public static final int MAX_TEXT_LENGTH = 2000;

    private final ThanksId id;
    private final CookId giver;
    private final HelpId help;
    private List<Recipient> recipients;
    private String text;
    private Picture picture;
    private final Instant createdAt;
    private Instant updatedAt;
    private final long version;

    private Thanks(ThanksId id, CookId giver, HelpId help, List<Recipient> recipients, String text, Picture picture,
                   Instant createdAt, Instant updatedAt, long version) {
        this.id = Objects.requireNonNull(id);
        this.giver = Objects.requireNonNull(giver);
        this.help = Objects.requireNonNull(help);
        this.recipients = checkedRecipients(recipients);
        this.text = checkedText(text);
        this.picture = Objects.requireNonNull(picture, "Thanks need a picture");
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.version = version;
    }

    public static Thanks give(CookId giver, HelpId help, List<Recipient> recipients, String text, Picture picture,
                              Instant now) {
        return new Thanks(ThanksId.newId(), giver, help, recipients, text, picture, now, now, 0);
    }

    /** Recreates stored thanks. */
    public static Thanks restore(ThanksId id, CookId giver, HelpId help, List<Recipient> recipients, String text,
                                 Picture picture, Instant createdAt, Instant updatedAt, long version) {
        return new Thanks(id, giver, help, recipients, text, picture, createdAt, updatedAt, version);
    }

    /** Applies the change; all or nothing. A revision changing nothing leaves even {@code updatedAt} as it is. */
    public void revise(ThanksRevision revision, Instant now) {
        if (revision.changesNothing()) {
            return;
        }
        List<Recipient> newRecipients = revision.changesRecipients() ? checkedRecipients(revision.recipients()) : recipients;
        String newText = revision.thanksText() != null ? checkedText(revision.thanksText()) : text;
        recipients = newRecipients;
        text = newText;
        if (revision.changesPicture()) {
            picture = revision.picture();
        }
        updatedAt = now;
    }

    public boolean isGivenBy(CookId cook) {
        return giver.equals(cook);
    }

    /** All cooks mentioned by any recipient, each once. */
    public Set<CookId> mentionedCooks() {
        return mentionedCooks(recipients);
    }

    public static Set<CookId> mentionedCooks(List<Recipient> recipients) {
        Set<CookId> cooks = new LinkedHashSet<>();
        recipients.forEach(recipient -> cooks.addAll(recipient.cooks()));
        return cooks;
    }

    private static List<Recipient> checkedRecipients(List<Recipient> recipients) {
        List<Recipient> checked = recipients == null ? List.of() : List.copyOf(recipients);
        Set<RecipientType> types = EnumSet.noneOf(RecipientType.class);
        for (Recipient recipient : checked) {
            if (!types.add(recipient.type())) {
                throw new ThanksRuleViolationException(DUPLICATE_RECIPIENT,
                        "Thanks name each type of recipient at most once, " + recipient.type() + " is named twice");
            }
        }
        return checked;
    }

    private static String checkedText(String text) {
        if (text == null || text.isBlank() || text.length() > MAX_TEXT_LENGTH) {
            throw new ThanksRuleViolationException(INVALID_THANKS_TEXT,
                    "A thanks text has 1 to " + MAX_TEXT_LENGTH + " characters and is not blank");
        }
        return text;
    }

    public ThanksId id() {
        return id;
    }

    public CookId giver() {
        return giver;
    }

    public HelpId help() {
        return help;
    }

    public List<Recipient> recipients() {
        return recipients;
    }

    public String text() {
        return text;
    }

    public Picture picture() {
        return picture;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public long version() {
        return version;
    }
}
