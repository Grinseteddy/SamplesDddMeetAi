package org.larder.grandmaavatar.adapter.in.messaging;

import java.io.IOException;
import java.util.Optional;

import org.larder.grandmaavatar.application.HelpRequestHandler;
import org.larder.grandmaavatar.application.HelpRequestHandler.Handling;
import org.larder.grandmaavatar.domain.HelpRequest;
import org.larder.grandmaavatar.domain.InvalidHelpRequestException;
import org.larder.platform.messaging.ContractMessage;
import org.larder.platform.messaging.MessageHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Consumes {@code HelpRequested} from queue {@code grandma-avatar.help-requested} (operation
 * {@code onHelpRequested}).
 * <p>
 * Failure handling:
 * <ul>
 *   <li><b>Permanent</b> - the message breaks the contract (headers without a valid correlationId or
 *       messageId, payload not JSON, missing fields, unknown enum values, invariants of the Help Request
 *       violated): logged and acknowledged. A retry would fail the same way, so it is not parked in the
 *       dead-letter queue either. The same holds for requests Grandma does not or cannot answer - those
 *       are acknowledged as a matter of course.</li>
 *   <li><b>Transient</b> - the database, the advisor ({@code AdvisorUnavailableException}) or anything
 *       else failing unexpectedly: the exception propagates, the platform's listener retries 3 times and
 *       then dead-letters the message into {@code grandma-avatar.help-requested.dlq}. Since nothing is
 *       committed before the end, a retry starts from scratch; a request already noted is not answered
 *       again.</li>
 * </ul>
 * The payload is read here (not by the message converter) so that a broken payload is a permanent
 * failure that is logged and acknowledged instead of a conversion error.
 */
public class HelpRequestedListener {

    private static final Logger LOG = LoggerFactory.getLogger(HelpRequestedListener.class);

    private final HelpRequestHandler handler;
    private final ObjectMapper objectMapper;

    public HelpRequestedListener(HelpRequestHandler handler, ObjectMapper objectMapper) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = HelpRequestedQueueConfiguration.QUEUE)
    public void onHelpRequested(Message message) {
        Optional<MessageHeader> header = header(message);
        Optional<HelpRequest> request = header.flatMap(it -> request(message, it));
        if (request.isEmpty()) {
            return;
        }
        Handling handling = handler.handle(request.get(), header.get().correlationId());
        if (handling.firstTime()) {
            LOG.info("Help request {} (correlation {}): {}", request.get().id().value(),
                    header.get().correlationId(), handling.handled().outcome());
        } else {
            LOG.info("Help request {} (message {}) was handled before, ignored", request.get().id().value(),
                    header.get().messageId());
        }
    }

    private static Optional<MessageHeader> header(Message message) {
        try {
            return Optional.of(ContractMessage.header(message));
        } catch (RuntimeException e) {
            LOG.warn("HelpRequested without valid contract headers acknowledged and dropped: {} {}",
                    message.getMessageProperties().getHeaders(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<HelpRequest> request(Message message, MessageHeader header) {
        try {
            HelpRequestedPayload payload = objectMapper.readValue(message.getBody(), HelpRequestedPayload.class);
            return Optional.of(HelpRequestedMapper.toDomain(payload));
        } catch (IOException | InvalidHelpRequestException e) {
            LOG.warn("HelpRequested {} (correlation {}) breaks the contract, acknowledged and dropped: {}",
                    header.messageId(), header.correlationId(), e.getMessage());
            return Optional.empty();
        }
    }
}
