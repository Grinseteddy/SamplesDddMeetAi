package org.larder.cookingassistance.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.larder.cookingassistance.application.GrandmaHelpOutcome;
import org.larder.cookingassistance.application.HelpService;
import org.larder.cookingassistance.application.NotFoundException;
import org.larder.cookingassistance.domain.HelpRuleViolationException;
import org.larder.platform.messaging.ContractMessage;
import org.larder.platform.messaging.MessageHeader;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.dao.DataAccessResourceFailureException;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Which failures are acknowledged (permanent) and which are retried / dead-lettered (transient). */
class GrandmaHelpProvidedListenerTest {

    private static final UUID CORRELATION_ID = UUID.fromString("23a8eeed-35f6-460b-892e-7bb458a8fded");

    private final HelpService helps = mock(HelpService.class);
    private final GrandmaHelpProvidedListener listener = new GrandmaHelpProvidedListener(helps);
    private final GrandmaHelpProvidedPayload payload;
    private final Message message = ContractMessage.of("HelpProvided",
            MessageHeader.newMessage(CORRELATION_ID, "grandma-avatar"), GrandmaMessages.STAY_CALM);

    GrandmaHelpProvidedListenerTest() throws Exception {
        payload = new ObjectMapper().readValue(GrandmaMessages.STAY_CALM, GrandmaHelpProvidedPayload.class);
    }

    @Test
    void recordsTheHelpUnderTheIncomingCorrelationId() {
        given(helps.receiveGrandmaHelp(any(), any())).willReturn(GrandmaHelpOutcome.RECORDED);

        listener.onHelpProvided(payload, message);

        then(helps).should().receiveGrandmaHelp(payload.toGrandmaHelp(), CORRELATION_ID);
    }

    @Test
    void aDuplicateIsAcknowledged() {
        given(helps.receiveGrandmaHelp(any(), any())).willReturn(GrandmaHelpOutcome.DUPLICATE);

        assertThatCode(() -> listener.onHelpProvided(payload, message)).doesNotThrowAnyException();
    }

    @Test
    void aWithdrawnRequestIsLoggedAndAcknowledged() {
        given(helps.receiveGrandmaHelp(any(), any())).willThrow(new NotFoundException("withdrawn"));

        assertThatCode(() -> listener.onHelpProvided(payload, message)).doesNotThrowAnyException();
    }

    @Test
    void aBrokenRuleIsLoggedAndAcknowledged() {
        given(helps.receiveGrandmaHelp(any(), any())).willThrow(new HelpRuleViolationException(
                HelpRuleViolationException.ANSWER_TYPE_MISMATCH, "wrong type"));

        assertThatCode(() -> listener.onHelpProvided(payload, message)).doesNotThrowAnyException();
    }

    @Test
    void aMessageWithoutTheContractHeadersIsLoggedAndAcknowledged() {
        Message withoutHeaders = new Message(GrandmaMessages.STAY_CALM.getBytes(), new MessageProperties());

        assertThatCode(() -> listener.onHelpProvided(payload, withoutHeaders)).doesNotThrowAnyException();
        then(helps).should(never()).receiveGrandmaHelp(any(), any());
    }

    @Test
    void aTransientFailureIsRethrownSoThePlatformRetriesAndDeadLetters() {
        given(helps.receiveGrandmaHelp(any(), any())).willThrow(new DataAccessResourceFailureException("database down"));

        assertThatThrownBy(() -> listener.onHelpProvided(payload, message))
                .isInstanceOf(DataAccessResourceFailureException.class);
    }
}
