package org.larder.platform.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

class TopologyTest {

    @Test
    void aConsumerQueueIsBoundToThePublishersExchangeAndDeadLettersIntoItsOwnQueue() {
        var declarables = Topology.consumerQueue("notifications.help-provided", "cooking-assistance", "cooking-assistance.help.provided");

        Queue queue = declarables.getDeclarablesByType(Queue.class).stream()
                .filter(q -> q.getName().equals("notifications.help-provided")).findFirst().orElseThrow();
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments()).containsEntry("x-dead-letter-exchange", Topology.DEAD_LETTER_EXCHANGE)
                .containsEntry("x-dead-letter-routing-key", "notifications.help-provided");
        assertThat(declarables.getDeclarablesByType(Queue.class)).extracting(Queue::getName)
                .contains("notifications.help-provided.dlq");
        assertThat(declarables.getDeclarablesByType(Binding.class)).extracting(Binding::getRoutingKey)
                .containsExactlyInAnyOrder("cooking-assistance.help.provided", "notifications.help-provided");
        assertThat(declarables.getDeclarablesByType(TopicExchange.class)).extracting(TopicExchange::getName)
                .containsExactly("cooking-assistance");
    }

    @Test
    void aContractMessageCarriesTheHeaderAndTypeButNoJavaClassName() {
        var header = MessageHeader.newMessage(UUID.randomUUID(), "cooking-assistance");

        var message = ContractMessage.of("HelpProvided", header, "{}");

        assertThat(ContractMessage.header(message)).isEqualTo(header);
        assertThat(ContractMessage.messageId(message)).isEqualTo(header.messageId());
        assertThat(ContractMessage.messageType(message)).isEqualTo("HelpProvided");
        assertThat(message.getMessageProperties().getHeaders()).containsAllEntriesOf(Map.of("source", "cooking-assistance"))
                .doesNotContainKey("__TypeId__");
    }
}
