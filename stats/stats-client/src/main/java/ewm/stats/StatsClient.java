package ewm.stats;

import ewm.client.BaseClient;
import ewm.dto.EndpointHitDTO;
import ewm.dto.StatsRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;

@Service
public class StatsClient extends BaseClient {
    private final DiscoveryClient discoveryClient;

    @Autowired
    public StatsClient(
            DiscoveryClient discoveryClient
    ) {
        super(new RestTemplate());
        this.discoveryClient = discoveryClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    private void init() {
        ServiceInstance serviceInstance = getInstance();
        this.rest.setUriTemplateHandler(new DefaultUriBuilderFactory("http://" + serviceInstance.getHost() + ":" + serviceInstance.getPort()));
    }

    private ServiceInstance getInstance() {
        try {
            return discoveryClient
                    .getInstances("stats-server")
                    .getFirst();
        } catch (Exception exception) {
            throw new RuntimeException(
                    "Ошибка обнаружения адреса сервиса с id: " + "stats-server",
                    exception
            );
        }
    }

    public ResponseEntity<Object> saveHit(EndpointHitDTO requestDto) {
        return post("/hit", requestDto);
    }

    public ResponseEntity<Object> getStats(StatsRequestDTO headersDto) {
        Map<String, Object> params = Map.of(
                "start", headersDto.getStart(),
                "end", headersDto.getEnd(),
                "unique", headersDto.getUnique()
        );
        return get("/stats" + getUrlParams(headersDto), params);
    }

    private String getUrlParams(StatsRequestDTO headersDto) {

        String urls = String.join(",", headersDto.getUris());
        return "?" + getKeyValueUrl("start", headersDto.getStart()) +
                "&" +
                getKeyValueUrl("end", headersDto.getEnd()) +
                "&" +
                getKeyValueUrl("uris", urls) +
                "&" +
                getKeyValueUrl("unique", headersDto.getUnique());
    }

    private String getKeyValueUrl(String key, Object value) {
        return key + "=" + value;
    }
}
