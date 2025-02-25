package ewm.category.service;

import ewm.category.dto.CategoryDto;
import ewm.category.dto.CreateCategoryDto;
import ewm.category.mapper.CategoryMapper;
import ewm.category.model.Category;
import ewm.category.repository.CategoryRepository;
import ewm.error.exception.ConflictException;
import ewm.error.exception.ExistException;
import ewm.error.exception.NotFoundException;
import ewm.event.EventRepository;
import ewm.event.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private static final String CATEGORY_NOT_FOUND = "Category not found";
    private static final String CATEGORY_NAME_EXIST = "Category with this name already exist";

    @Override
    public List<CategoryDto> getAll(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        return categoryRepository.findAll(pageable).stream().map(CategoryMapper.INSTANCE::categoryToCategoryDto).toList();
    }

    @Override
    public CategoryDto getById(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            throw new NotFoundException(CATEGORY_NOT_FOUND);
        }
        return CategoryMapper.INSTANCE.categoryToCategoryDto(category.get());
    }

    @Override
    public CategoryDto add(CreateCategoryDto createCategoryDto) {
        try {
            Category category = categoryRepository.save(Category.builder().name(createCategoryDto.getName()).build());
            return CategoryMapper.INSTANCE.categoryToCategoryDto(category);
        } catch (DataAccessException e) {
            throw new ExistException(CATEGORY_NAME_EXIST);
        }
    }

    @Override
    public CategoryDto update(Long id, CreateCategoryDto createCategoryDto) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            throw new NotFoundException(CATEGORY_NOT_FOUND);
        }
        Category categoryToUpdate = category.get();
        categoryToUpdate.setName(createCategoryDto.getName());
        try {
            Category updated = categoryRepository.save(categoryToUpdate);
            return CategoryMapper.INSTANCE.categoryToCategoryDto(updated);
        } catch (DataAccessException e) {
            throw new ExistException(CATEGORY_NAME_EXIST);
        }
    }

    @Override
    public void delete(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            throw new NotFoundException(CATEGORY_NOT_FOUND);
        }
        List<Event> events = eventRepository.findByCategoryId(id);
        if (!events.isEmpty()) throw new ConflictException("Есть привязанные события.");
        categoryRepository.deleteById(id);
    }
}
