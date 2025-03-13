package ewm.event.controller.pub;

import ewm.event.dto.EventDto;
import ewm.event.dto.PublicGetEventRequestDto;
import ewm.event.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventDto> publicGetEvents(HttpServletRequest request, PublicGetEventRequestDto requestParams) {
        log.info("Получить события, согласно устловиям -> {}", requestParams);
        return eventService.publicGetEvents(requestParams, request);
    }

    @GetMapping("/{id}")
    public EventDto publicGetEvent(@PathVariable Long id,
                                   HttpServletRequest request) {
        return eventService.publicGetEvent(id, request);
    }

    @GetMapping("/public/{eventId}")
    public EventDto getById(@PathVariable Long eventId) {
        return eventService.getEventById(eventId);
    }

    @PutMapping("/{id}")
    EventDto updateEvent(@PathVariable Long id, @RequestBody EventDto event) {
        return eventService.update(id, event);
    }

    @GetMapping("/category/{categoryId}")
    public List<EventDto> findAllByCategoryId(@PathVariable Long categoryId) {
        return eventService.findAllByCategoryId(categoryId);
    }

    @GetMapping("/public")
    public List<EventDto> findAllByIds(@RequestParam List<Long> ids) {
        return eventService.findAllByIds(ids);
    }
}
