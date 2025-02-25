package ewm.stats.mapper;

import ewm.dto.EndpointHitResponseDto;
import ewm.stats.model.Hit;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface HitMapper {
    HitMapper INSTANCE = Mappers.getMapper(HitMapper.class);

    EndpointHitResponseDto hitToEndpointHitResponseDto(Hit hit);
}
