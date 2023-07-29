package ru.yandex.practicum.mainservice.category.service.admin.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mainservice.category.dto.CategoryDto;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.category.repository.CategoryRepository;
import ru.yandex.practicum.mainservice.category.service.admin.CategoryAdminService;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;

import static ru.yandex.practicum.mainservice.category.dto.CategoryMapper.toCategory;
import static ru.yandex.practicum.mainservice.category.dto.CategoryMapper.toCategoryDto;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryAdminServiceImpl implements CategoryAdminService {

    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public CategoryDto addCategory(CategoryDto categoryDto) {
        CategoryDto response = toCategoryDto(categoryRepository.save(toCategory(categoryDto)));
        log.info("A new Category has created: {}", response);
        return response;
    }

    @Transactional
    @Override
    public CategoryDto update(Long categoryId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (categoryDto.getName() != null) {
            category.setName(categoryDto.getName());
        }
        log.info("User {} has updated his data.", categoryId);
        return toCategoryDto(categoryRepository.save(category));
    }

    @Transactional
    @Override
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                new EntityNotFoundException(String.format("Category with id=%s was not found", categoryId)));
        categoryRepository.delete(category);
        log.info("Category {} has been deleted.", category);
    }

}
