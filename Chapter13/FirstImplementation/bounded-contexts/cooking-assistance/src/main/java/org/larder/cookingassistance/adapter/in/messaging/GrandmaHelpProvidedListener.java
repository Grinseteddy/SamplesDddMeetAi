package org.larder.cookingassistance.adapter.in.messaging;

import org.larder.cookingassistance.application.GrandmaHelpOutcome;
import org.larder.cookingassistance.application.HelpService;
import org.larder.cookingassistance.application.NotFoundException;
import org.larder.cookingassistance.domain.HelpRuleViolationException;
import org.larder.platform.messaging.ContractMessage;
import org.larder.platform.messaging.MessageHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumes the Grandma Avatar's answers (operation {@code consumeGrandmaHelpProvided}) and records each
 * one as a help - through the same use case as a help given over REST, so the same rules apply, the
 * request becomes Answered and HelpProvided is published via the outbox under the incoming correlationId.
 *
 * <p>Failure handling (delivery is at least once; acknowledging happens when this method returns):
 * <ul>
 *   <li><b>Redelivery</b> of a help already recorded (same {@code helpId}) changes nothing and is acknowledged.</li>
 *   <li><b>Permanent rejections</b> - the request no longer exists (withdrawn), the answer breaks a rule of the
 *       request (other type, other recipe, avatar not a preferred provider, ...) or the message is incomplete -
 *       are logged and acknowledged: retrying cannot make them succeed, and parking them in the dead-letter
 *       queue would only hide a business outcome as an operational error.</li>
 *   <li><b>Transient failures</b> (database or broker down) propagate: the platform retries 3 times and then
 *       dead-letters into {@code cooking-assistance.grandma-avatar-help-provided.dlq}. A message that cannot
 *       even be converted (not JSON) takes the same path.</li>
 * </ul>
 */
@Component("cookingassistanceGrandmaHelpProvidedListener")
class GrandmaHelpProvidedListener {

    private static final Logger LOG = LoggerFactory.getLogger(GrandmaHelpProvidedListener.class);

    private final HelpService helps;

    GrandmaHelpProvidedListener(HelpService helps) {
        this.helps = helps;
    }

    @RabbitListener(queues = GrandmaHelpProvidedQueueConfiguration.QUEUE)
    void onHelpProvided(@Payload GrandmaHelpProvidedPayload payload, Message message) {
        String messageId = message.getMessageProperties().getMessageId();
        try {
            MessageHeader header = ContractMessage.header(message);
            GrandmaHelpOutcome outcome = helps.receiveGrandmaHelp(payload.toGrandmaHelp(), header.correlationId());
            if (outcome == GrandmaHelpOutcome.DUPLICATE) {
                LOG.info("Grandma Avatar help {} for request {} was received before (message {}), ignored",
                        payload.helpId(), payload.helpRequest(), messageId);
            }
        } catch (NotFoundException e) {
            LOG.warn("Grandma Avatar help {} rejected (message {}): {}", payload.helpId(), messageId, e.getMessage());
        } catch (HelpRuleViolationException e) {
            LOG.warn("Grandma Avatar help {} rejected with {} (message {}): {}",
                    payload.helpId(), e.code(), messageId, e.getMessage());
        } catch (IllegalArgumentException e) {
            // incomplete payload, unknown enum value or missing MessageHeader
            LOG.warn("Grandma Avatar help {} rejected as malformed (message {}): {}",
                    payload.helpId(), messageId, e.getMessage());
        }
    }
}
