package ewm;

import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AggregatorService {
    private final KafkaTemplate<String, GenericRecord> kafkaTemplate;
    // Храним максимальные веса действий пользователей для каждого мероприятия
    private final Map<Long, Map<Long, Double>> userEventWeights = new HashMap<>();
    // Храним суммы весов для каждого мероприятия
    private final Map<Long, Double> eventWeightSums = new HashMap<>();

    @Value("${kafka.topics.events-similarity}")
    private String eventSimilarityTopic;

    @KafkaListener(topics = "${kafka.topics.user-action}")
    public void consume(UserActionAvro userActionAvro) {
        long userId = userActionAvro.getUserId();
        long eventId = userActionAvro.getEventId();
        double weight = switch (userActionAvro.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };

        updateWeights(userId, eventId, weight);
        recalculateSimilarity(eventId, userActionAvro.getTimestamp());
    }

    private void updateWeights(long userId, long eventId, double newWeight) {
        userEventWeights.computeIfAbsent(eventId, e -> new HashMap<>())
                .merge(userId, newWeight, Math::max);

        // Пересчитываем сумму весов для мероприятия
        double totalWeight = userEventWeights.get(eventId).values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        eventWeightSums.put(eventId, totalWeight);
    }

    private void recalculateSimilarity(long eventA, long timestamp) {
        for (long eventB : eventWeightSums.keySet()) {
            if (eventA == eventB) continue;  // Пропускаем сравнение самого с собой

            long first = Math.min(eventA, eventB);
            long second = Math.max(eventA, eventB);

            double sMin = userEventWeights.getOrDefault(first, new HashMap<>())
                    .entrySet().stream()
                    .filter(entry -> userEventWeights.getOrDefault(second, new HashMap<>()).containsKey(entry.getKey()))
                    .mapToDouble(entry -> Math.min(entry.getValue(), userEventWeights.get(second).get(entry.getKey())))
                    .sum();

            double sA = eventWeightSums.getOrDefault(first, 0.0);
            double sB = eventWeightSums.getOrDefault(second, 0.0);

            if (sA > 0 && sB > 0) {
                double similarity = sMin / (sA * sB);
                sendSimilarityToKafka(first, second, similarity, timestamp);
            }
        }
    }

    private void sendSimilarityToKafka(long eventA, long eventB, double similarity, long timestamp) {
        EventSimilarityAvro eventSimilarity = EventSimilarityAvro.newBuilder()
                .setEventA(eventA)
                .setEventB(eventB)
                .setScore((float) similarity)
                .setTimestamp(timestamp)
                .build();

        kafkaTemplate.send(eventSimilarityTopic, eventSimilarity);
    }
}
