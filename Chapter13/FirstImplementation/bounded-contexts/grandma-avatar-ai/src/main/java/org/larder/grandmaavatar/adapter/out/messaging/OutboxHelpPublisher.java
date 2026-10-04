package org.larder.grandmaavatar.adapter.out.messaging;

import java.util.UUID;

import org.larder.grandmaavatar.application.HelpPublisher;
import org.larder.grandmaavatar.domain.Help;
import org.larder.platform.messaging.Outbox;

/**
 * Publishes {@code HelpProvided} on the {@code grandma-avatar} exchange through the transactional
 * outbox: the message is written in the caller's transaction and sent later by the outbox relay.
 */
public class OutboxHelpPublisher implements HelpPublisher {

    public static final String EXCHANGE = GrandmaAvatarExchangeConfiguration.EXCHANGE;
    public static final String ROUTING_KEY = GrandmaAvatarExchangeConfiguration.HELP_PROVIDED;
    public static final String MESSAGE_TYPE = "HelpProvided";

    private final Outbox outbox;

    public OutboxHelpPublisher(Outbox outbox) {
        this.outbox = outbox;
    }

    @Override
    public void helpProvided(Help help, UUID correlationId) {
        outbox.add(EXCHANGE, ROUTING_KEY, MESSAGE_TYPE, correlationId, HelpProvidedMapper.toPayload(help));
    }
}
