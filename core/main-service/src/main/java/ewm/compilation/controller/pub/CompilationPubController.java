package ewm.compilation.controller.pub;

import ewm.compilation.dto.CompilationDtoResponse;
import ewm.compilation.service.CompilationService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class CompilationPubController {
	private final CompilationService compilationService;

	@GetMapping
	public List<CompilationDtoResponse> getCompilations(@RequestParam(required = false) Boolean pinned,
														@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
														@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		log.info("Получить список подборок по pinned --> {}, from --> {}, size --> {}", pinned, from, size);
		return compilationService.getCompilations(pinned, from, size);
	}

	@GetMapping("/{compId}")
	public CompilationDtoResponse getCompilations(@PathVariable Long compId) {
		log.info("Получить подбороку по compId --> {}", compId);
		return compilationService.getCompilation(compId);
	}
}
