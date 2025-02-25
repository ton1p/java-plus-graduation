package ewm.event.controller.pub;

import ewm.event.EventService;
import ewm.event.dto.EventDto;
import ewm.event.dto.PublicGetEventRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	EventDto publicGetEvent(@PathVariable Long id,
							HttpServletRequest request) {
		return eventService.publicGetEvent(id, request);
	}
}
