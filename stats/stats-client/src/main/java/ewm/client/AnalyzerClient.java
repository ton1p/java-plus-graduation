package ewm.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.RecommendationsControllerGrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.ewm.stats.avro.Recommendations.*;

@Component
public class AnalyzerClient {
    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerStub;

    public List<RecommendedEventProto> getRecommendations(long userId, int maxResults) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        List<RecommendedEventProto> recommendations = new ArrayList<>();
        analyzerStub.getRecommendationsForUser(request)
                .forEachRemaining(recommendations::add);

        return recommendations;
    }

    public List<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        List<RecommendedEventProto> similarEvents = new ArrayList<>();
        analyzerStub.getSimilarEvents(request)
                .forEachRemaining(similarEvents::add);

        return similarEvents;
    }

    public Map<Long, Double> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();

        Map<Long, Double> interactionsCount = new HashMap<>();
        analyzerStub.getInteractionsCount(request)
                .forEachRemaining(event -> interactionsCount.put(event.getEventId(), (double) event.getScore()));

        return interactionsCount;
    }
}
