package org.larder.cookingassistance.adapter.out.messaging;

import static org.larder.cookingassistance.adapter.out.messaging.CookingAssistanceExchangeConfiguration.EXCHANGE;
import static org.larder.cookingassistance.adapter.out.messaging.CookingAssistanceExchangeConfiguration.HELP_PROVIDED;
import static org.larder.cookingassistance.adapter.out.messaging.CookingAssistanceExchangeConfiguration.HELP_REQUESTED;

import java.util.UUID;

import org.larder.cookingassistance.application.HelpEvents;
import org.larder.cookingassistance.domain.Help;
import org.larder.cookingassistance.domain.HelpRequest;
import org.larder.platform.messaging.Outbox;

/**
 * Publishes HelpRequested and HelpProvided through the transactional outbox: the message is written in
 * the use case's transaction and relayed to the {@code cooking-assistance} exchange after the commit.
 */
class OutboxHelpEvents implements HelpEvents {

    static final String HELP_REQUESTED_MESSAGE = "HelpRequested";
    static final String HELP_PROVIDED_MESSAGE = "HelpProvided";

    private final Outbox outbox;

    OutboxHelpEvents(Outbox outbox) {
        this.outbox = outbox;
    }

    @Override
    public void helpRequested(HelpRequest request) {
        outbox.add(EXCHANGE, HELP_REQUESTED, HELP_REQUESTED_MESSAGE, request.id().value(), HelpRequestedPayload.of(request));
    }

    @Override
    public void helpProvided(Help help, UUID correlationId) {
        outbox.add(EXCHANGE, HELP_PROVIDED, HELP_PROVIDED_MESSAGE, correlationId, HelpProvidedPayload.of(help));
    }
}
