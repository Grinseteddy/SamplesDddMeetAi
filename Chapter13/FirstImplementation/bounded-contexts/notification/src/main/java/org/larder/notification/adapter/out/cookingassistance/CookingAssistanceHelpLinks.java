package org.larder.notification.adapter.out.cookingassistance;

import java.net.URI;
import java.util.UUID;

import org.larder.notification.application.HelpLinks;

/** Links to a Help as Cooking Assistance publishes it: {@code <base-url>/cooking-assistance/helps/{helpId}}. */
class CookingAssistanceHelpLinks implements HelpLinks {

    private final String baseUrl;

    CookingAssistanceHelpLinks(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        URI.create(this.baseUrl);
    }

    @Override
    public URI helpLink(UUID helpId) {
        return URI.create(baseUrl + "/cooking-assistance/helps/" + helpId);
    }
}
