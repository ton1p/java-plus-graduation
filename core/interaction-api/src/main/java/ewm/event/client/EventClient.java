package ewm.event.client;

import ewm.event.dto.EventDto;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "event-service")
public interface EventClient {
    @Retryable(retryFor = {FeignException.class}, maxAttempts = 5, backoff = @Backoff(delay = 2000))
    @GetMapping("/events/public/{eventId}")
    EventDto getById(@PathVariable Long eventId);


    @PutMapping("/events/{id}")
    EventDto updateEvent(@PathVariable Long id, @RequestBody EventDto event);

    @GetMapping("/events/category/{categoryId}")
    List<EventDto> findAllByCategoryId(@PathVariable Long categoryId);

    @GetMapping("/events/public")
    List<EventDto> findAllByIds(@RequestParam List<Long> ids);
}
