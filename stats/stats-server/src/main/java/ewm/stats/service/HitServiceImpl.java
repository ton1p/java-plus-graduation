package ewm.stats.service;

import ewm.dto.EndpointHitDTO;
import ewm.dto.EndpointHitResponseDto;
import ewm.dto.StatsRequestDTO;
import ewm.dto.ViewStatsDTO;
import ewm.stats.error.expection.BadRequestExceptions;
import ewm.stats.mapper.HitMapper;
import ewm.stats.model.Hit;
import ewm.stats.repository.HitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {
    private final HitRepository hitRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ERROR_MESSAGE = "You need to pass start and end dates";

    @Override
    @Transactional
    public EndpointHitResponseDto create(EndpointHitDTO endpointHitDTO) {
        Hit hit = Hit.builder()
                .ip(endpointHitDTO.getIp())
                .app(endpointHitDTO.getApp())
                .uri(endpointHitDTO.getUri())
                .timestamp(LocalDateTime.parse(endpointHitDTO.getTimestamp(), FORMATTER))
                .build();

        Hit saved = hitRepository.save(hit);
        return HitMapper.INSTANCE.hitToEndpointHitResponseDto(saved);
    }

    @Override
    public List<ViewStatsDTO> getAll(StatsRequestDTO statsRequestDTO) {
        if (statsRequestDTO.getStart() == null || statsRequestDTO.getEnd() == null) {
            throw new BadRequestExceptions(ERROR_MESSAGE);
        }
        LocalDateTime start = LocalDateTime.parse(statsRequestDTO.getStart(), FORMATTER);
        LocalDateTime end = LocalDateTime.parse(statsRequestDTO.getEnd(), FORMATTER);
        if (start.isAfter(end)) {
            throw new BadRequestExceptions("Start date must be before end date");
        }
        Boolean isUriExist = !statsRequestDTO.getUris().isEmpty();
        return Boolean.TRUE.equals(statsRequestDTO.getUnique()) ?
                convertHitsToViewStatsDTO(hitRepository.findAllUniqueBetweenDatesAndByUri(start, end, isUriExist, statsRequestDTO.getUris())) :
                convertHitsToViewStatsDTO(hitRepository.findAllBetweenDatesAndByUri(start, end, isUriExist, statsRequestDTO.getUris()));
    }

    private List<ViewStatsDTO> convertHitsToViewStatsDTO(List<HitRepository.ResponseHit> hits) {
        return hits.stream().map(responseHit -> ViewStatsDTO.builder()
                .app(responseHit.getApp())
                .uri(responseHit.getUri())
                .hits(responseHit.getHits()).build()
        ).toList();
    }
}
