package org.larder.notification.adapter.in.messaging;

import java.util.List;
import java.util.UUID;

import org.larder.notification.application.HelpRequested;
import org.larder.notification.domain.EventId;
import org.larder.notification.domain.HelpProvider;
import org.larder.notification.domain.ReceiverId;
import org.larder.platform.messaging.MessageHeader;

/** {@code HelpRequestedPayload} of {@code cooking-assistance.asyncapi.yaml}, the published language Notification conforms to. */
record HelpRequestedPayload(
        UUID helpRequestId,
        UUID requester,
        String title,
        HelpType type,
        String description,
        UUID recipe,
        UUID howToStep,
        List<UUID> ingredients,
        List<HelpProviderType> preferredProvider,
        String status) {

    /** @throws org.larder.notification.application.InvalidEventException if a field Notification needs is missing */
    HelpRequested toEvent(MessageHeader header) {
        return new HelpRequested(
                new EventId(header.messageId()),
                helpRequestId,
                requester == null ? null : new ReceiverId(requester),
                preferredProvider == null ? null : preferredProvider.stream()
                        .map(provider -> provider == null ? null : HelpProvider.valueOf(provider.name()))
                        .toList());
    }
}
