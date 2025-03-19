package ewm;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserAction;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.avro.UserActionControllerGrpc;

@GrpcService
public class CollectorServer extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final KafkaTemplate<String, GenericRecord> kafkaTemplate;
    private final String topic;


    public CollectorServer(
            KafkaTemplate<String, GenericRecord> kafkaTemplate,
            @Value("${kafka.topics.user-action}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void collectUserAction(UserAction.UserActionProto request, StreamObserver<com.google.protobuf.Empty> responseObserver) {
        UserActionAvro userAction = UserActionAvro.newBuilder()
                .setUserId(request.getUserId())
                .setEventId(request.getEventId())
                .setActionType(mapActionType(request.getActionType()))
                .setTimestamp(request.getTimestamp().getSeconds() * 1000)
                .build();

        kafkaTemplate.send(new ProducerRecord<>(topic, String.valueOf(request.getUserId()), userAction));
        responseObserver.onNext(com.google.protobuf.Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private ActionTypeAvro mapActionType(UserAction.ActionTypeProto type) {
        return switch (type) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> throw new IllegalArgumentException("Unknown action type: " + type);
        };
    }
}
