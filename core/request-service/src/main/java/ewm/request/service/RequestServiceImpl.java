package ewm.request.service;

import ewm.client.CollectorClient;
import ewm.error.exception.ConflictException;
import ewm.error.exception.NotFoundException;
import ewm.event.client.EventClient;
import ewm.event.dto.EventDto;
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
    private final UserClient userClient;
    private final EventClient eventClient;
    private final CollectorClient collectorClient;

    @Override
    public List<RequestDto> getRequests(Long userId) {
        getUser(userId);
        return RequestMapper.INSTANCE.mapListRequests(requestRepository.findAllByRequester(userId));
    }

    @Transactional
    @Override
    public RequestDto createRequest(Long userId, Long eventId) {
        EventDto event = getEvent(eventId);
        UserDto user = getUser(userId);
        checkRequest(userId, event);
        Request request = Request.builder().requester(user.getId()).created(LocalDateTime.now()).status(!event.getRequestModeration() || event.getParticipantLimit() == 0 ? RequestStatus.CONFIRMED : RequestStatus.PENDING).event(event.getId()).build();
        request = requestRepository.save(request);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventClient.updateEvent(event.getId(), event);
        }
        collectorClient.sendEventRegistration(userId, eventId);
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
        EventDto event = getEvent(request.getEvent());
        event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        eventClient.updateEvent(event.getId(), event);
        return RequestMapper.INSTANCE.mapToRequestDto(request);
    }

    @Override
    public List<RequestDto> getAllByEvent(Long eventId) {
        return RequestMapper.INSTANCE.mapListRequests(requestRepository.findAllByEvent(eventId));
    }

    @Override
    public List<RequestDto> getAllByIds(List<Long> ids) {
        return RequestMapper.INSTANCE.mapListRequests(requestRepository.findAllById(ids));
    }

    @Override
    @Transactional
    public List<RequestDto> updateAll(List<RequestDto> requestDtoList) {
        List<Request> requests = requestDtoList
                .stream()
                .map((requestDto -> new Request(
                        requestDto.getId(),
                        requestDto.getCreated(),
                        requestDto.getEvent(),
                        requestDto.getRequester(),
                        requestDto.getStatus()
                )))
                .toList();
        requestRepository.saveAll(requests);
        return RequestMapper.INSTANCE.mapListRequests(requests);
    }

    private void checkRequest(Long userId, EventDto event) {
        if (!requestRepository.findAllByRequesterAndEvent(userId, event.getId()).isEmpty())
            throw new ConflictException("нельзя добавить повторный запрос");
        if (event.getInitiator().getId().equals(userId))
            throw new ConflictException("инициатор события не может добавить запрос на участие в своём событии");
        if (!event.getState().equals(EventState.PUBLISHED))
            throw new ConflictException("нельзя участвовать в неопубликованном событии");
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit().equals(event.getConfirmedRequests()))
            throw new ConflictException("у события достигнут лимит запросов на участие");
    }

    private EventDto getEvent(Long eventId) {
        return eventClient.getById(eventId);
    }

    private UserDto getUser(Long userId) {
        return userClient.findById(userId);
    }

    private Request getRequest(Long requestId) {
        Optional<Request> request = requestRepository.findById(requestId);
        if (request.isEmpty()) throw new NotFoundException("Запроса с id = " + requestId + " не существует");
        return request.get();
    }
}
