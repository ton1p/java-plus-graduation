package ewm;

import ewm.model.EventSimilarity;
import ewm.model.UserActionHistory;
import ewm.repository.EventSimilarityRepository;
import ewm.repository.UserActionHistoryRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.Recommendations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionHistoryRepository userActionHistoryRepository;

    public void getSimilarEvents(
            Recommendations.SimilarEventsRequestProto request,
            StreamObserver<Recommendations.RecommendedEventProto> responseObserver
    ) {
        List<EventSimilarity> similarities = eventSimilarityRepository.findByEventAOrEventB(request.getEventId(), request.getEventId());

        Set<Long> userInteractions = userActionHistoryRepository.findByUserId(request.getUserId())
                .stream()
                .map(UserActionHistory::getEventId)
                .collect(Collectors.toSet());

        similarities.stream()
                .filter(sim -> !userInteractions.contains(sim.getEventA()) || !userInteractions.contains(sim.getEventB()))
                .sorted(Comparator.comparing(EventSimilarity::getScore).reversed())
                .limit(request.getMaxResults())
                .forEach(sim -> {
                    long recommendedEvent = sim.getEventA().equals(request.getEventId()) ? sim.getEventB() : sim.getEventA();
                    responseObserver.onNext(Recommendations.RecommendedEventProto.newBuilder()
                            .setEventId(recommendedEvent)
                            .setScore(sim.getScore())
                            .build());
                });

        responseObserver.onCompleted();
    }

    public void getRecommendationsForUser(
            Recommendations.UserPredictionsRequestProto request,
            StreamObserver<Recommendations.RecommendedEventProto> responseObserver
    ) {
        List<UserActionHistory> interactions = userActionHistoryRepository.findByUserId(request.getUserId());

        if (interactions.isEmpty()) {
            responseObserver.onCompleted();
            return;
        }

        Set<Long> recentEvents = interactions.stream()
                .sorted(Comparator.comparing(UserActionHistory::getTimestamp).reversed())
                .limit(request.getMaxResults())
                .map(UserActionHistory::getEventId)
                .collect(Collectors.toSet());

        List<EventSimilarity> similarities = new ArrayList<>();
        for (Long eventId : recentEvents) {
            similarities.addAll(eventSimilarityRepository.findByEventAOrEventB(eventId, eventId));
        }

        Set<Long> userInteractions = interactions.stream().map(UserActionHistory::getEventId).collect(Collectors.toSet());

        similarities.stream()
                .filter(sim -> !userInteractions.contains(sim.getEventA()) || !userInteractions.contains(sim.getEventB()))
                .sorted(Comparator.comparing(EventSimilarity::getScore).reversed())
                .limit(request.getMaxResults())
                .forEach(sim -> {
                    long recommendedEvent = userInteractions.contains(sim.getEventA()) ? sim.getEventB() : sim.getEventA();
                    responseObserver.onNext(Recommendations.RecommendedEventProto.newBuilder()
                            .setEventId(recommendedEvent)
                            .setScore(sim.getScore())
                            .build());
                });

        responseObserver.onCompleted();
    }

    public void getInteractionsCount(
            Recommendations.InteractionsCountRequestProto request,
            StreamObserver<Recommendations.RecommendedEventProto> responseObserver
    ) {
        for (long eventId : request.getEventIdList()) {
            double totalWeight = userActionHistoryRepository.findByUserId(eventId)
                    .stream()
                    .mapToDouble((uah) -> switch (uah.getActionType()) {
                        case VIEW -> 0.4;
                        case REGISTER -> 0.8;
                        case LIKE -> 1.0;
                    })
                    .sum();

            responseObserver.onNext(Recommendations.RecommendedEventProto.newBuilder()
                    .setEventId(eventId)
                    .setScore((float) totalWeight)
                    .build());
        }

        responseObserver.onCompleted();
    }
}
