package ru.yandex.practicum.mainservice.category.controller.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.mainservice.category.dto.CategoryDto;
import ru.yandex.practicum.mainservice.category.service.publics.CategoryPublicService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/categories")

public class CategoryPublicController {

    private final CategoryPublicService categoryService;

    @GetMapping
    public List<CategoryDto> categories(@RequestParam(defaultValue = "0") int from,
                                        @RequestParam(defaultValue = "10") int size) {
        return categoryService.getAll(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto categoryById(@PathVariable Long catId) {
        return categoryService.get(catId);
    }
}
