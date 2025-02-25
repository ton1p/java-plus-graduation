package ewm.category.service;

import ewm.category.dto.CategoryDto;
import ewm.category.dto.CreateCategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto getById(Long id);

    CategoryDto add(CreateCategoryDto createCategoryDto);

    CategoryDto update(Long id, CreateCategoryDto createCategoryDto);

    void delete(Long id);
}
