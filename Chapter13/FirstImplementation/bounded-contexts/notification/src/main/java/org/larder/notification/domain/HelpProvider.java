package org.larder.notification.domain;

/** Who answered a help request, as named in the published language of Cooking Assistance. */
public enum HelpProvider {
    CHEF("a Chef"),
    GRANDMA_AVATAR("the Grandma Avatar"),
    COMMUNITY("the Community");

    private final String phrase;

    HelpProvider(String phrase) {
        this.phrase = phrase;
    }

    /** How the provider is named in a sentence, e.g. "the Grandma Avatar". */
    public String phrase() {
        return phrase;
    }
}
