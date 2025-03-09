package ewm.event.mapper;

import ewm.category.mapper.CategoryMapper;
import ewm.event.dto.CreateEventDto;
import ewm.event.dto.EventDto;
import ewm.event.dto.LocationDto;
import ewm.event.model.Event;
import ewm.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class EventMapper {

    public static Event mapCreateDtoToEvent(CreateEventDto dto) {
        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setCreatedOn(LocalDateTime.now());
        event.setDescription(dto.getDescription());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(dto.getEventDate(), formatter);
        event.setEventDate(dateTime);
        event.setPaid(dto.getPaid());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.getRequestModeration());
        event.setTitle(dto.getTitle());
        event.setLat(dto.getLocation().getLat());
        event.setLon(dto.getLocation().getLon());
        return event;
    }

    public static EventDto mapEventToEventDto(Event event, UserDto userDto) {
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .eventDate(event.getEventDate())
                .annotation(event.getAnnotation())
                .paid(event.getPaid())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .state(event.getState().toString())
                .participantLimit(event.getParticipantLimit())
                .location(LocationDto.builder().lat(event.getLat()).lon(event.getLon()).build())
                .category(CategoryMapper.INSTANCE.categoryToCategoryDto(event.getCategory()))
                .initiator(userDto)
                .requestModeration(event.getRequestModeration())
                .views((event.getViews() == null) ? 0L : event.getViews())
                .confirmedRequests(event.getConfirmedRequests())
                .build();
    }

    public static List<EventDto> mapToEventDto(Iterable<Event> events, Function<Long, UserDto> getUserFunction) {
        List<EventDto> dtos = new ArrayList<>();
        for (Event event : events) {
            UserDto userDto = getUserFunction.apply(event.getInitiator());
            dtos.add(mapEventToEventDto(event, userDto));
        }
        return dtos;
    }
}
