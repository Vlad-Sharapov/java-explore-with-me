package ru.yandex.practicum.mainservice.category.service.publics.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mainservice.category.dto.CategoryDto;
import ru.yandex.practicum.mainservice.category.dto.CategoryMapper;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.category.repository.CategoryRepository;
import ru.yandex.practicum.mainservice.category.service.publics.CategoryPublicService;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.mainservice.category.dto.CategoryMapper.toCategoryDto;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryPublicServiceImpl implements CategoryPublicService {

    private final CategoryRepository categoryRepository;


    @Override
    public CategoryDto get(long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Category with id - %s not found", catId)));
        log.info("Category {} is being viewed", catId);
        return toCategoryDto(category);
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        Page<Category> allUsers = categoryRepository.findAll(pageRequest);
        log.info("A list of all categories has been received.");
        return allUsers.stream()
                .map((CategoryMapper::toCategoryDto))
                .collect(Collectors.toList());
    }
}
