package ewm;

import ewm.deserializer.EventSimilarityAvroDeserializer;
import ewm.deserializer.UserActionsAvroDeserializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Map;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class KafkaConfig {
    private final AnalyzerKafkaListener analyzerKafkaListener;
    @Value("${kafka.bootstrap-address}")
    private String bootstrapAddress;
    @Value("${kafka.topics.user-action}")
    private String userActionTopic;
    @Value("${kafka.topics.events-similarity}")
    private String eventsSimilarityTopic;

    @Bean
    public ConsumerFactory<String, EventSimilarityAvro> eventSimilarityConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfig("event-similarity-group-id"), new StringDeserializer(), new EventSimilarityAvroDeserializer());
    }

    @Bean
    public ConsumerFactory<String, UserActionAvro> userActionsConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfig("user-actions-group-id"), new StringDeserializer(), new UserActionsAvroDeserializer());
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, EventSimilarityAvro> eventSimilarityListenerContainer() {
        ContainerProperties containerProps = new ContainerProperties(eventsSimilarityTopic);
        containerProps.setMessageListener((MessageListener<String, EventSimilarityAvro>) record -> analyzerKafkaListener.eventsSimilarityReceived(record.value()));
        return new ConcurrentMessageListenerContainer<>(eventSimilarityConsumerFactory(), containerProps);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, UserActionAvro> userActionsListenerContainer() {
        ContainerProperties containerProps = new ContainerProperties(userActionTopic);
        containerProps.setMessageListener((MessageListener<String, UserActionAvro>) record -> analyzerKafkaListener.userActionReceived(record.value()));
        return new ConcurrentMessageListenerContainer<>(userActionsConsumerFactory(), containerProps);
    }

    private Map<String, Object> consumerConfig(final String groupId) {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress,
                ConsumerConfig.GROUP_ID_CONFIG, groupId
        );
    }
}
