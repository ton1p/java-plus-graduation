package ewm.event.client;

import ewm.event.dto.EventDto;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// todo change name to event-service after extract
@FeignClient(name = "main-service", path = "/events")
public interface EventClient {
    @Retryable(retryFor = {FeignException.class}, maxAttempts = 5, backoff = @Backoff(delay = 2000))
    @GetMapping("/public/{eventId}")
    EventDto getById(@PathVariable Long eventId);
}
