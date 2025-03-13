package ewm.request.client;

import ewm.request.dto.RequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "request-service", path = "/requests")
public interface RequestClient {
    @GetMapping("/event/{eventId}")
    List<RequestDto> findAllByEvent(@PathVariable Long eventId);

    @GetMapping
    List<RequestDto> findAllById(@RequestParam List<Long> ids);

    @PutMapping
    List<RequestDto> updateAll(@RequestBody List<RequestDto> requestDtoList);
}
