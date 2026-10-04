package org.larder.notification.adapter.in.messaging;

import java.util.UUID;

import org.larder.notification.application.HelpProvided;
import org.larder.notification.domain.EventId;
import org.larder.notification.domain.HelpProvider;
import org.larder.notification.domain.ReceiverId;
import org.larder.platform.messaging.MessageHeader;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@code HelpProvidedPayload} of {@code cooking-assistance.asyncapi.yaml}, the published language
 * Notification conforms to. The typed {@code answer} is taken as it comes; Notification does not read it.
 */
record HelpProvidedPayload(
        UUID helpId,
        UUID helpRequest,
        UUID helpRequester,
        String answerTitle,
        HelpProviderType helpProviderType,
        UUID helpProvider,
        JsonNode answer,
        String helpRequestStatus) {

    /** @throws org.larder.notification.application.InvalidEventException if a field Notification needs is missing */
    HelpProvided toEvent(MessageHeader header) {
        return new HelpProvided(
                new EventId(header.messageId()),
                helpId,
                helpRequester == null ? null : new ReceiverId(helpRequester),
                answerTitle,
                helpProviderType == null ? null : HelpProvider.valueOf(helpProviderType.name()));
    }
}
