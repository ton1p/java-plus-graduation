package ewm.request.mapper;

import ewm.request.dto.RequestDto;
import ewm.request.model.Request;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RequestMapper {
    RequestMapper INSTANCE = Mappers.getMapper(RequestMapper.class);

    RequestDto mapToRequestDto(Request request);

    List<RequestDto> mapListRequests(List<Request> requests);
}
