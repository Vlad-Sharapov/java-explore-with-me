package ru.yandex.practicum.mainservice.category.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.mainservice.category.dto.CategoryDto;
import ru.yandex.practicum.mainservice.category.service.admin.CategoryAdminService;

import javax.validation.Valid;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/admin/categories")
public class CategoryAdminController {

    private final CategoryAdminService categoryAdminService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto add(@Valid @RequestBody CategoryDto categoryDto) {
        return categoryAdminService.addCategory(categoryDto);
    }

    @PatchMapping("/{catId}")
    public CategoryDto update(@PathVariable long catId,
                              @Valid @RequestBody CategoryDto categoryDto) {
        return categoryAdminService.update(catId, categoryDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long catId) {
        categoryAdminService.deleteCategory(catId);
    }
}
