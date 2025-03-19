package ewm.event.service;

import ewm.category.client.CategoryClient;
import ewm.category.dto.CategoryDto;
import ewm.client.AnalyzerClient;
import ewm.client.CollectorClient;
import ewm.error.exception.ConflictException;
import ewm.error.exception.NotFoundException;
import ewm.error.exception.ValidationException;
import ewm.event.dto.*;
import ewm.event.mapper.EventMapper;
import ewm.event.model.Event;
import ewm.event.model.EventState;
import ewm.event.model.StateAction;
import ewm.event.repository.EventRepository;
import ewm.request.client.RequestClient;
import ewm.request.dto.RequestDto;
import ewm.request.model.RequestStatus;
import ewm.user.client.UserClient;
import ewm.user.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private static final String EVENT_NOT_FOUND_MESSAGE = "Event not found";
    private final EventRepository repository;
    private final UserClient userClient;
    private final RequestClient requestClient;
    private final CategoryClient categoryClient;

    private final CollectorClient collectorClient;
    private final AnalyzerClient analyzerClient;

    @Override
    public List<EventDto> getEvents(Long userId, Integer from, Integer size) {
        UserDto userDto = getUser(userId);
        Pageable pageable = PageRequest.of(from, size);
        return repository.findByInitiator(userId, pageable).stream()
                .map(event -> eventToDto(event, userDto, getCategory(event.getCategory())))
                .toList();
    }

    @Override
    public EventDto getEventById(Long userId, Long id, String ip, String uri) {
        UserDto userDto = getUser(userId);
        Optional<Event> event = repository.findByIdAndInitiator(id, userId);
        if (event.isEmpty()) {
            throw new NotFoundException(EVENT_NOT_FOUND_MESSAGE);
        }
        Event found = event.get();
        collectorClient.sendEventView(userId, found.getId());
        return eventToDto(found, userDto, getCategory(found.getCategory()));
    }

    @Override
    public EventDto getEventById(Long eventId) {
        Optional<Event> eventOptional = repository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new NotFoundException(EVENT_NOT_FOUND_MESSAGE);
        }
        Event event = eventOptional.get();
        UserDto userDto = getUser(event.getInitiator());
        return eventToDto(event, userDto, getCategory(event.getCategory()));
    }

    @Override
    @Transactional
    public EventDto createEvent(Long userId, CreateEventDto eventDto) {
        UserDto userDto = getUser(userId);
        CategoryDto category = getCategory(eventDto.getCategory());
        Event event = EventMapper.mapCreateDtoToEvent(eventDto);
        if (event.getPaid() == null) {
            event.setPaid(false);
        }
        if (event.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        }
        if (event.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }

        event.setInitiator(userDto.getId());
        event.setCategory(category.getId());
        event.setState(EventState.PENDING);
        Event newEvent = repository.save(event);
        return eventToDto(newEvent, userDto, category);
    }

    @Override
    @Transactional
    public EventDto updateEvent(Long userId, UpdateEventDto eventDto, Long eventId) {
        UserDto userDto = getUser(userId);
        Optional<Event> eventOptional = repository.findById(eventId);
        if (eventOptional.isEmpty()) {
            throw new NotFoundException(EVENT_NOT_FOUND_MESSAGE);
        }
        Event foundEvent = eventOptional.get();
        if (foundEvent.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя изменять изменять событие, которое опубликовано");
        }
        updateEventFields(eventDto, foundEvent);
        Event saved = repository.save(foundEvent);
        return eventToDto(saved, userDto, getCategory(saved.getCategory()));
    }

    @Override
    @Transactional
    public List<EventDto> publicGetEvents(PublicGetEventRequestDto requestParams,
                                          HttpServletRequest request) {
        LocalDateTime start = (requestParams.getRangeStart() == null) ?
                LocalDateTime.now() : requestParams.getRangeStart();
        LocalDateTime end = (requestParams.getRangeEnd() == null) ?
                LocalDateTime.now().plusYears(10) : requestParams.getRangeEnd();

        if (start.isAfter(end))
            throw new ValidationException("Дата окончания, должна быть больше даты старта.");
        List<Event> events = repository.findEventsPublic(
                requestParams.getText(),
                requestParams.getCategories(),
                requestParams.getPaid(),
                start,
                end,
                EventState.PUBLISHED,
                requestParams.getOnlyAvailable(),
                PageRequest.of(requestParams.getFrom() / requestParams.getSize(),
                        requestParams.getSize())
        );

        return EventMapper.mapToEventDto(events, this::getUser, this::getCategory);
    }

    @Override
    @Transactional
    public EventDto publicGetEvent(Long userId, Long id, HttpServletRequest request) {
        Event event = getEvent(id);
        UserDto userDto = getUser(event.getInitiator());
        CategoryDto category = getCategory(event.getCategory());
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие не найдено");
        }
        collectorClient.sendEventView(userId, event.getId());
        event = repository.save(event);
        return EventMapper.mapEventToEventDto(event, userDto, category);
    }

    @Override
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        getUser(userId);
        getEvent(eventId);
        return requestClient.findAllByEvent(eventId);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeStatusEventRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        getUser(userId);
        Event event = getEvent(eventId);
        EventRequestStatusUpdateResult response = new EventRequestStatusUpdateResult();
        List<RequestDto> requests = requestClient.findAllById(request.getRequestIds());
        if (request.getStatus().equals(RequestStatus.REJECTED)) {
            checkRequestsStatus(requests);
            requests.forEach(tmpReq -> changeStatus(tmpReq, RequestStatus.REJECTED));
            requestClient.updateAll(requests);
            response.setRejectedRequests(requests);
        } else {
            if (requests.size() + event.getConfirmedRequests() > event.getParticipantLimit())
                throw new ConflictException("Превышен лимит заявок");
            requests.forEach(tmpReq -> changeStatus(tmpReq, RequestStatus.CONFIRMED));
            requestClient.updateAll(requests);
            event.setConfirmedRequests(event.getConfirmedRequests() + requests.size());
            repository.save(event);
            response.setConfirmedRequests(requests);
        }
        return response;
    }

    @Override
    @Transactional
    public EventDto update(Long id, EventDto eventDto) {
        Event event = getEvent(id);
        Event updatedEvent = new Event(
                eventDto.getId(),
                event.getAnnotation(),
                eventDto.getCreatedOn(),
                eventDto.getDescription(),
                eventDto.getEventDate(),
                eventDto.getPaid(),
                eventDto.getParticipantLimit(),
                eventDto.getPublishedOn(),
                eventDto.getRequestModeration(),
                eventDto.getState(),
                eventDto.getTitle(),
                event.getCategory(),
                eventDto.getInitiator().getId(),
                eventDto.getLocation().getLat(),
                eventDto.getLocation().getLon(),
                0.0, // todo add rating
                eventDto.getConfirmedRequests()
        );
        return EventMapper.mapEventToEventDto(repository.save(updatedEvent), eventDto.getInitiator(), eventDto.getCategory());
    }

    @Override
    public List<EventDto> findAllByCategoryId(Long categoryId) {
        return EventMapper.mapToEventDto(repository.findAllByCategory(categoryId), this::getUser, this::getCategory);
    }

    @Override
    public List<EventDto> findAllByIds(List<Long> ids) {
        return EventMapper.mapToEventDto(repository.findAllById(ids), this::getUser, this::getCategory);
    }

    @Override
    public List<EventDto> getRecommendations(long userId, int maxResults) {
        return analyzerClient.getRecommendations(userId, maxResults).stream()
                .sorted((a, b) -> (int) (a.getScore() - b.getScore()))
                .map((r) -> {
                    Event event = getEvent(r.getEventId());
                    UserDto userDto = getUser(event.getInitiator());
                    CategoryDto category = getCategory(event.getCategory());
                    return eventToDto(event, userDto, category);
                }).toList();
    }

    @Override
    public void likeEvent(Long eventId, long userId) {
        getEvent(eventId);
        collectorClient.sendEventLike(userId, eventId);
    }

    @Override
    public List<EventDto> adminGetEvents(AdminGetEventRequestDto requestParams) {
        List<Event> events = repository.findEventsByAdmin(
                requestParams.getUsers(),
                requestParams.getStates(),
                requestParams.getCategories(),
                requestParams.getRangeStart(),
                requestParams.getRangeEnd(),
                PageRequest.of(requestParams.getFrom() / requestParams.getSize(),
                        requestParams.getSize())
        );
        return EventMapper.mapToEventDto(events, this::getUser, this::getCategory);
    }

    @Override
    @Transactional
    public EventDto adminChangeEvent(Long eventId, UpdateEventDto updateEventDto) {
        Event event = getEvent(eventId);
        UserDto userDto = getUser(event.getInitiator());
        CategoryDto category = getCategory(event.getCategory());
        checkEventForUpdate(event, updateEventDto.getStateAction());
        Event updatedEvent = repository.save(prepareEventForUpdate(event, updateEventDto));
        return EventMapper.mapEventToEventDto(updatedEvent, userDto, category);
    }

    private EventDto eventToDto(Event event, UserDto userDto, CategoryDto category) {
        return EventMapper.mapEventToEventDto(event, userDto, category);
    }

    private Event getEvent(Long eventId) {
        return repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND_MESSAGE));
    }

    private void checkEventForUpdate(Event event, StateAction action) {
        checkEventDate(event.getEventDate());
        if (action == null) return;
        if (action.equals(StateAction.PUBLISH_EVENT)
                && !event.getState().equals(EventState.PENDING))
            throw new ConflictException("Опубликовать событие можно в статусе PENDING, а статус = "
                    + event.getState());
        if (action.equals(StateAction.REJECT_EVENT)
                && event.getState().equals(EventState.PUBLISHED))
            throw new ConflictException("Отменить событие можно только в статусе PUBLISHED, а статус = "
                    + event.getState());
    }

    private Event prepareEventForUpdate(Event event, UpdateEventDto updateEventDto) {
        if (updateEventDto.getAnnotation() != null)
            event.setAnnotation(updateEventDto.getAnnotation());
        if (updateEventDto.getDescription() != null)
            event.setDescription(updateEventDto.getDescription());
        if (updateEventDto.getEventDate() != null) {
            checkEventDate(updateEventDto.getEventDate());
            event.setEventDate(updateEventDto.getEventDate());
        }
        if (updateEventDto.getPaid() != null)
            event.setPaid(updateEventDto.getPaid());
        if (updateEventDto.getParticipantLimit() != null)
            event.setParticipantLimit(updateEventDto.getParticipantLimit());
        if (updateEventDto.getTitle() != null)
            event.setTitle(updateEventDto.getTitle());
        if (updateEventDto.getStateAction() != null) {
            switch (updateEventDto.getStateAction()) {
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case CANCEL_REVIEW, REJECT_EVENT:
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
            }
        }
        return event;
    }

    private void checkEventDate(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now().plusHours(1)))
            throw new ConflictException("Дата начала события меньше чем час " + dateTime);
    }

    private UserDto getUser(Long userId) {
        return userClient.findById(userId);
    }

    private CategoryDto getCategory(Long categoryId) {
        return categoryClient.getCategory(categoryId);
    }


    private void updateEventFields(UpdateEventDto eventDto, Event foundEvent) {
        if (eventDto.getCategory() != null) {
            CategoryDto category = getCategory(eventDto.getCategory());
            foundEvent.setCategory(category.getId());
        }

        if (eventDto.getAnnotation() != null && !eventDto.getAnnotation().isBlank()) {
            foundEvent.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getDescription() != null && !eventDto.getDescription().isBlank()) {
            foundEvent.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            if (eventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Дата начала события не может быть раньше чем через 2 часа");
            }
            foundEvent.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getPaid() != null) {
            foundEvent.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            if (eventDto.getParticipantLimit() < 0) {
                throw new ValidationException("Participant limit cannot be negative");
            }
            foundEvent.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            foundEvent.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getTitle() != null && !eventDto.getTitle().isBlank()) {
            foundEvent.setTitle(eventDto.getTitle());
        }
        if (eventDto.getLocation() != null) {
            if (eventDto.getLocation().getLat() != null) {
                foundEvent.setLat(eventDto.getLocation().getLat());
            }
            if (eventDto.getLocation().getLon() != null) {
                foundEvent.setLon(eventDto.getLocation().getLon());
            }
        }

        if (eventDto.getStateAction() != null) {
            switch (eventDto.getStateAction()) {
                case CANCEL_REVIEW -> foundEvent.setState(EventState.CANCELED);
                case PUBLISH_EVENT -> foundEvent.setState(EventState.PUBLISHED);
                case SEND_TO_REVIEW -> foundEvent.setState(EventState.PENDING);
            }
        }

        if (eventDto.getConfirmedRequests() != null) {
            foundEvent.setConfirmedRequests(eventDto.getConfirmedRequests());
        }
    }

    private void changeStatus(RequestDto request, RequestStatus status) {
        request.setStatus(status);
    }

    private void checkRequestsStatus(List<RequestDto> requests) {
        Optional<RequestDto> confirmedReq = requests.stream()
                .filter(request -> request.getStatus() == RequestStatus.CONFIRMED)
                .findFirst();
        if (confirmedReq.isPresent())
            throw new ConflictException("Нельзя отменить, уже принятую заявку.");
    }
}
