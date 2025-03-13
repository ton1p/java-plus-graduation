package ewm.request.controller;

import ewm.request.client.RequestClient;
import ewm.request.dto.RequestDto;
import ewm.request.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestPublicController implements RequestClient {
    private final RequestService requestService;

    @Override
    @GetMapping("/event/{eventId}")
    public List<RequestDto> findAllByEvent(@PathVariable Long eventId) {
        return requestService.getAllByEvent(eventId);
    }

    @Override
    @GetMapping
    public List<RequestDto> findAllById(List<Long> ids) {
        return requestService.getAllByIds(ids);
    }

    @Override
    @PutMapping
    public List<RequestDto> updateAll(List<RequestDto> requestDtoList) {
        return requestService.updateAll(requestDtoList);
    }
}
