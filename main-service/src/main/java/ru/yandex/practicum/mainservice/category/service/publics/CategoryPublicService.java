package ru.yandex.practicum.mainservice.category.service.publics;

import ru.yandex.practicum.mainservice.category.dto.CategoryDto;

import java.util.List;

public interface CategoryPublicService {

    CategoryDto get(long catId);

    List<CategoryDto> getAll(int from, int size);

}
