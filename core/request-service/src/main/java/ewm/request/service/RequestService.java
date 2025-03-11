package ewm.request.service;

import ewm.request.dto.RequestDto;

import java.util.List;

public interface RequestService {
    List<RequestDto> getRequests(Long userId);

    RequestDto createRequest(Long userId, Long eventId);

    RequestDto cancelRequest(Long userId, Long requestId);

    List<RequestDto> getAllByEvent(Long eventId);

    List<RequestDto> getAllByIds(List<Long> ids);

    List<RequestDto> updateAll(List<RequestDto> requestDtoList);
}
