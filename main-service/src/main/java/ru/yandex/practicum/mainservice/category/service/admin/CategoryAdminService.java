package ru.yandex.practicum.mainservice.category.service.admin;

import ru.yandex.practicum.mainservice.category.dto.CategoryDto;

public interface CategoryAdminService {

    CategoryDto addCategory(CategoryDto categoryDto);

    CategoryDto update(Long categoryId, CategoryDto categoryDto);

    void deleteCategory(Long categoryId);

}
