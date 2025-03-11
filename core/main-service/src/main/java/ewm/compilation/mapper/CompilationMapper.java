package ewm.compilation.mapper;

import ewm.compilation.dto.CompilationDtoResponse;
import ewm.compilation.model.Compilation;
import ewm.event.dto.EventDto;

import java.util.List;
import java.util.function.Function;

public class CompilationMapper {
    public static CompilationDtoResponse compilationToCompilationDtoResponse(Compilation compilation, Function<Long, EventDto> getEventDtoFn) {
        List<EventDto> events = compilation.getEvents().stream().map(getEventDtoFn).toList();
        return new CompilationDtoResponse(compilation.getId(), events, compilation.getPinned(), compilation.getTitle());
    }

    public static List<CompilationDtoResponse> mapListCompilations(List<Compilation> compilations, Function<Long, EventDto> getEventDtoFn) {
        return compilations.stream().map((compilation) -> compilationToCompilationDtoResponse(compilation, getEventDtoFn)).toList();
    }
}
