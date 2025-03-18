package ewm.event.mapper;

import ewm.category.dto.CategoryDto;
import ewm.event.dto.CreateEventDto;
import ewm.event.dto.EventDto;
import ewm.event.dto.LocationDto;
import ewm.event.model.Event;
import ewm.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class EventMapper {

    public static Event mapCreateDtoToEvent(CreateEventDto dto) {
        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setCreatedOn(LocalDateTime.now());
        event.setDescription(dto.getDescription());
        LocalDateTime dateTime = dto.getEventDate();
        event.setEventDate(dateTime);
        event.setPaid(dto.getPaid());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.getRequestModeration());
        event.setTitle(dto.getTitle());
        event.setLat(dto.getLocation().getLat());
        event.setLon(dto.getLocation().getLon());
        return event;
    }

    public static EventDto mapEventToEventDto(Event event, UserDto userDto, CategoryDto categoryDto) {
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .eventDate(event.getEventDate())
                .annotation(event.getAnnotation())
                .paid(event.getPaid())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .state(event.getState())
                .participantLimit(event.getParticipantLimit())
                .location(LocationDto.builder().lat(event.getLat()).lon(event.getLon()).build())
                .category(categoryDto)
                .initiator(userDto)
                .requestModeration(event.getRequestModeration())
                .confirmedRequests(event.getConfirmedRequests())
                .build();
    }

    public static List<EventDto> mapToEventDto(Iterable<Event> events, Function<Long, UserDto> getUserFunction, Function<Long, CategoryDto> getCategoryFunction) {
        List<EventDto> list = new ArrayList<>();
        for (Event event : events) {
            UserDto userDto = getUserFunction.apply(event.getInitiator());
            CategoryDto categoryDto = getCategoryFunction.apply(event.getCategory());
            list.add(mapEventToEventDto(event, userDto, categoryDto));
        }
        return list;
    }
}
