package ewm.request.service;

import ewm.error.exception.ConflictException;
import ewm.error.exception.NotFoundException;
import ewm.event.EventRepository;
import ewm.event.model.Event;
import ewm.event.model.EventState;
import ewm.request.dto.RequestDto;
import ewm.request.mapper.RequestMapper;
import ewm.request.model.Request;
import ewm.request.model.RequestStatus;
import ewm.request.repository.RequestRepository;
import ewm.user.client.UserClient;
import ewm.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserClient userClient;

    @Override
    public List<RequestDto> getRequests(Long userId) {
        getUser(userId);
        return RequestMapper.INSTANCE.mapListRequests(requestRepository.findAllByRequester(userId));
    }

    @Transactional
    @Override
    public RequestDto createRequest(Long userId, Long eventId) {
        Event event = getEvent(eventId);
        UserDto user = getUser(userId);
        checkRequest(userId, event);
        Request request = Request.builder()
                .requester(user.getId())
                .created(LocalDateTime.now())
                .status(!event.getRequestModeration()
                        || event.getParticipantLimit() == 0
                        ? RequestStatus.CONFIRMED : RequestStatus.PENDING)
                .event(event)
                .build();
        request = requestRepository.save(request);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        return RequestMapper.INSTANCE.mapToRequestDto(request);
    }

    @Transactional
    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        getUser(userId);
        Request request = getRequest(requestId);
        if (!request.getRequester().equals(userId))
            throw new ConflictException("Другой пользователь не может отменить запрос");
        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
        Event event = getEvent(request.getEvent().getId());
        event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        eventRepository.save(event);
        return RequestMapper.INSTANCE.mapToRequestDto(request);
    }

    private void checkRequest(Long userId, Event event) {
        if (!requestRepository.findAllByRequesterAndEvent_id(userId, event.getId()).isEmpty())
            throw new ConflictException("нельзя добавить повторный запрос");
        if (event.getInitiator().equals(userId))
            throw new ConflictException("инициатор события не может добавить запрос на участие в своём событии");
        if (!event.getState().equals(EventState.PUBLISHED))
            throw new ConflictException("нельзя участвовать в неопубликованном событии");
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit().equals(event.getConfirmedRequests()))
            throw new ConflictException("у события достигнут лимит запросов на участие");
    }

    private Event getEvent(Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty())
            throw new NotFoundException("События с id = " + eventId + " не существует");
        return event.get();
    }

    private UserDto getUser(Long userId) {
        return userClient.findById(userId);
    }

    private Request getRequest(Long requestId) {
        Optional<Request> request = requestRepository.findById(requestId);
        if (request.isEmpty())
            throw new NotFoundException("Запроса с id = " + requestId + " не существует");
        return request.get();
    }
}
