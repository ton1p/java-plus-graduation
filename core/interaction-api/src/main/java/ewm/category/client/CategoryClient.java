package ewm.category.client;

import ewm.category.dto.CategoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "main-service", path = "/categories")
public interface CategoryClient {
    @GetMapping("/{categoryId}")
    CategoryDto getCategory(@PathVariable Long categoryId);
}
