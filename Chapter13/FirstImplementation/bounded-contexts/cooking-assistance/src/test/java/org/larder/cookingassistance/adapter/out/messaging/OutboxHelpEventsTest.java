package org.larder.cookingassistance.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.larder.cookingassistance.TestData.COOK;
import static org.larder.cookingassistance.TestData.NOW;
import static org.larder.cookingassistance.TestData.burningCatastrophe;
import static org.larder.cookingassistance.TestData.stayCalm;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.cookingassistance.domain.Help;
import org.larder.cookingassistance.domain.HelpId;
import org.larder.cookingassistance.domain.HelpProviderType;
import org.larder.cookingassistance.domain.HelpRequest;
import org.larder.platform.messaging.Outbox;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.larder.platform.test.TestDatabase;

import com.fasterxml.jackson.databind.ObjectMapper;

/** The events land in this context's outbox table with the contract's exchange, routing key and name. */
class OutboxHelpEventsTest {

    private BoundedContextDatabase database;
    private OutboxHelpEvents events;

    @BeforeEach
    void freshSchema() {
        database = TestDatabase.forSchema("cookingassistance");
        events = new OutboxHelpEvents(new Outbox(database.jdbcClient(), new ObjectMapper(),
                CookingAssistanceMessagingConfiguration.SOURCE));
    }

    private Map<String, Object> theOnlyOutboxRow() {
        return database.jdbcClient().sql("select * from outbox").query().singleRow();
    }

    @Test
    void helpRequestedStartsAJourneyTracedByTheRequestId() {
        HelpRequest request = HelpRequest.raise(COOK, burningCatastrophe(), NOW);

        events.helpRequested(request);

        Map<String, Object> row = theOnlyOutboxRow();
        assertThat(row).containsEntry("exchange", "cooking-assistance")
                .containsEntry("routing_key", "cooking-assistance.help.requested")
                .containsEntry("message_type", "HelpRequested")
                .containsEntry("source", "cooking-assistance")
                .containsEntry("correlation_id", request.id().value());
        assertThat((String) row.get("payload")).contains(request.id().value().toString());
    }

    @Test
    void helpProvidedKeepsTheGivenCorrelationId() {
        HelpRequest request = HelpRequest.raise(COOK, burningCatastrophe(), NOW);
        Help help = request.answer(HelpId.newId(), HelpProviderType.GRANDMA_AVATAR, null, "Stay calm", stayCalm(), NOW);
        UUID correlationId = UUID.randomUUID();

        events.helpProvided(help, correlationId);

        assertThat(theOnlyOutboxRow()).containsEntry("routing_key", "cooking-assistance.help.provided")
                .containsEntry("message_type", "HelpProvided")
                .containsEntry("correlation_id", correlationId);
    }
}
