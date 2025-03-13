package ewm.compilation.service;

import ewm.compilation.dto.CompilationDto;
import ewm.compilation.dto.CompilationDtoResponse;
import ewm.compilation.dto.CompilationDtoUpdate;
import ewm.compilation.mapper.CompilationMapper;
import ewm.compilation.model.Compilation;
import ewm.compilation.repositry.CompilationRepository;
import ewm.error.exception.NotFoundException;
import ewm.event.client.EventClient;
import ewm.event.dto.EventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventClient eventClient;

    @Transactional
    @Override
    public CompilationDtoResponse createCompilation(CompilationDto compilationDto) {
        List<EventDto> events = compilationDto.getEvents() == null ?
                new ArrayList<>() : eventClient.findAllByIds(compilationDto.getEvents());
        Compilation compilation = Compilation.builder()
                .events(events.stream().map(EventDto::getId).toList())
                .title(compilationDto.getTitle())
                .pinned(compilationDto.getPinned())
                .build();
        return CompilationMapper.compilationToCompilationDtoResponse(compilationRepository.save(compilation), eventClient::getById);
    }

    @Transactional
    @Override
    public void deleteCompilation(Long compId) {
        getCompFromRepo(compId);
        compilationRepository.deleteById(compId);
    }

    @Transactional
    @Override
    public CompilationDtoResponse updateCompilation(Long compId, CompilationDtoUpdate compilationDto) {
        Compilation compilation = getCompFromRepo(compId);
        if (compilationDto.getEvents() != null && !compilationDto.getEvents().isEmpty()) {
            compilation.setEvents(eventClient.findAllByIds(compilationDto.getEvents()).stream().map(EventDto::getId).collect(Collectors.toList()));
        }
        if (compilationDto.getPinned() != null) compilation.setPinned(compilationDto.getPinned());
        if (compilationDto.getTitle() != null) compilation.setTitle(compilationDto.getTitle());
        return CompilationMapper.compilationToCompilationDtoResponse(compilationRepository.save(compilation), eventClient::getById);
    }

    @Override
    public List<CompilationDtoResponse> getCompilations(Boolean pinned, Integer from, Integer size) {
        int page = from / size;
        Pageable pageRequest = PageRequest.of(page, size);
        if (pinned != null)
            return CompilationMapper.mapListCompilations(compilationRepository.findByPinned(pinned, pageRequest), eventClient::getById);
        return CompilationMapper.mapListCompilations(compilationRepository.findAll(pageRequest).getContent(), eventClient::getById);
    }

    @Override
    public CompilationDtoResponse getCompilation(Long compId) {
        return CompilationMapper.compilationToCompilationDtoResponse(getCompFromRepo(compId), eventClient::getById);
    }

    private Compilation getCompFromRepo(Long compId) {
        Optional<Compilation> compilation = compilationRepository.findById(compId);
        if (compilation.isEmpty())
            throw new NotFoundException("Подборки с id = " + compId + " не существует");
        return compilation.get();
    }
}
