package ewm;

import ewm.model.EventSimilarity;
import ewm.model.UserActionHistory;
import ewm.model.UserActionType;
import ewm.repository.EventSimilarityRepository;
import ewm.repository.UserActionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzerKafkaListener {
    private final UserActionHistoryRepository userActionHistoryRepository;
    private final EventSimilarityRepository eventSimilarityRepository;

    public void userActionReceived(UserActionAvro userActionAvro) {
        log.info("Received UserActionAvro: {}", userActionAvro);
        LocalDateTime localDateTime = Instant.ofEpochMilli(userActionAvro.getTimestamp()).atZone(ZoneId.of("UTC")).toLocalDateTime();
        UserActionType actionType = UserActionType.valueOf(userActionAvro.getActionType().name());
        UserActionHistory userActionHistory = UserActionHistory.builder()
                .userId(userActionAvro.getUserId())
                .eventId(userActionAvro.getEventId())
                .actionType(actionType)
                .timestamp(localDateTime)
                .build();
        userActionHistoryRepository.save(userActionHistory);
    }

    public void eventsSimilarityReceived(EventSimilarityAvro eventSimilarityAvro) {
        log.info("Received EventSimilarityAvro: {}", eventSimilarityAvro);
        LocalDateTime localDateTime = Instant.ofEpochMilli(eventSimilarityAvro.getTimestamp()).atZone(ZoneId.of("UTC")).toLocalDateTime();
        EventSimilarity eventSimilarity = EventSimilarity.builder()
                .eventA(eventSimilarityAvro.getEventA())
                .eventB(eventSimilarityAvro.getEventB())
                .score(eventSimilarityAvro.getScore())
                .timestamp(localDateTime)
                .build();
        eventSimilarityRepository.save(eventSimilarity);
    }
}
