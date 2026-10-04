package org.larder.sharing.domain;

import static org.larder.sharing.domain.ThanksRuleViolationException.INVALID_RECIPIENT;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * One party thanks are addressed to (value object inside {@link Thanks}).
 *
 * <ul>
 *   <li>Type {@code COOK} mentions at least one cook, each cook once, and carries no chef name.</li>
 *   <li>Type {@code CHEF} may carry a chef name (1-200 characters, not blank) and mentions no cooks.</li>
 *   <li>Type {@code GRANDMA_AVATAR} carries neither a chef name nor cooks.</li>
 * </ul>
 */
public record Recipient(RecipientId id, RecipientType type, String chefName, List<CookId> cooks) {

    public static final int MAX_CHEF_NAME_LENGTH = 200;

    public Recipient {
        Objects.requireNonNull(id, "recipientId must not be null");
        if (type == null) {
            throw new ThanksRuleViolationException(INVALID_RECIPIENT, "A recipient needs a type");
        }
        cooks = cooks == null ? List.of() : List.copyOf(cooks);
        if (chefName != null && type != RecipientType.CHEF) {
            throw new ThanksRuleViolationException(INVALID_RECIPIENT, "Only a recipient of type Chef carries a chef name");
        }
        if (chefName != null && (chefName.isBlank() || chefName.length() > MAX_CHEF_NAME_LENGTH)) {
            throw new ThanksRuleViolationException(INVALID_RECIPIENT,
                    "A chef name has 1 to " + MAX_CHEF_NAME_LENGTH + " characters and is not blank");
        }
        if (!cooks.isEmpty() && type != RecipientType.COOK) {
            throw new ThanksRuleViolationException(INVALID_RECIPIENT, "Only a recipient of type Cook mentions cooks");
        }
        if (type == RecipientType.COOK && cooks.isEmpty()) {
            throw new ThanksRuleViolationException(INVALID_RECIPIENT, "A recipient of type Cook mentions at least one cook");
        }
        if (new LinkedHashSet<>(cooks).size() != cooks.size()) {
            throw new ThanksRuleViolationException(INVALID_RECIPIENT, "A recipient mentions each cook only once");
        }
    }

    /** A new recipient as the giver names it; the identifier is assigned here. */
    public static Recipient name(RecipientType type, String chefName, List<CookId> cooks) {
        return new Recipient(RecipientId.newId(), type, chefName, cooks);
    }

    public static Recipient cooks(CookId... cooks) {
        return name(RecipientType.COOK, null, List.of(cooks));
    }

    public static Recipient chef(String chefName) {
        return name(RecipientType.CHEF, chefName, List.of());
    }

    public static Recipient grandmaAvatar() {
        return name(RecipientType.GRANDMA_AVATAR, null, List.of());
    }

    public Optional<String> chef() {
        return Optional.ofNullable(chefName);
    }
}
