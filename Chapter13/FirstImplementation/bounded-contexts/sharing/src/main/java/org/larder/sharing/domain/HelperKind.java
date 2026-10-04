package org.larder.sharing.domain;

/** Who gave a help, as Sharing understands Cooking Assistance's help provider type. */
public enum HelperKind {

    /** A community cook; the help names the cook. */
    COMMUNITY_COOK(RecipientType.COOK),
    /** A cook answering as chef; the help names the cook, not the chef's name. */
    CHEF(RecipientType.CHEF),
    /** The Grandma Avatar; the help names nobody. */
    GRANDMA_AVATAR(RecipientType.GRANDMA_AVATAR);

    private final RecipientType thankedAs;

    HelperKind(RecipientType thankedAs) {
        this.thankedAs = thankedAs;
    }

    /** The recipient type under which this kind of helper is thanked. */
    public RecipientType thankedAs() {
        return thankedAs;
    }
}
