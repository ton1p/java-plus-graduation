package ewm.category.controller;

import ewm.category.dto.CategoryDto;
import ewm.category.dto.CreateCategoryDto;
import ewm.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CategoryDto addCategory(@RequestBody @Valid CreateCategoryDto createCategoryDto) {
        return categoryService.add(createCategoryDto);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCategory(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
    }

    @PatchMapping("/{categoryId}")
    CategoryDto updateCategory(@PathVariable Long categoryId, @RequestBody @Valid CreateCategoryDto createCategoryDto) {
        return categoryService.update(categoryId, createCategoryDto);
    }
}
