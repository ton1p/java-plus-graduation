package ewm.compilation.service;

import ewm.compilation.dto.CompilationDto;
import ewm.compilation.dto.CompilationDtoResponse;
import ewm.compilation.dto.CompilationDtoUpdate;

import java.util.List;

public interface CompilationService {
	CompilationDtoResponse createCompilation(CompilationDto compilationDto);

	void deleteCompilation(Long compId);

	CompilationDtoResponse updateCompilation(Long compId, CompilationDtoUpdate compilationDto);

	List<CompilationDtoResponse> getCompilations(Boolean pinned, Integer from, Integer size);

	CompilationDtoResponse getCompilation(Long compId);
}
