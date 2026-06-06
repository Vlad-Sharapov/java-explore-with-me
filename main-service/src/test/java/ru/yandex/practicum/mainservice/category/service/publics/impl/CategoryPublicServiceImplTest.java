package ru.yandex.practicum.mainservice.category.service.publics.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.mainservice.category.dto.CategoryDto;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.category.repository.CategoryRepository;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CategoryPublicServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryPublicServiceImpl categoryPublicService;

    @Test
    void shouldGetCategory() {
        Category category = Category.builder()
                .id(1L)
                .name("Concerts")
                .build();

        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(category));

        CategoryDto result = categoryPublicService.get(category.getId());

        Mockito.verify(categoryRepository).findById(category.getId());

        assertThat(result.getId(), equalTo(category.getId()));
        assertThat(result.getName(), equalTo(category.getName()));
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> categoryPublicService.get(111L));

        Mockito.verify(categoryRepository).findById(111L);
    }

    @Test
    void shouldGetAllCategories() {
        Category category1 = Category.builder()
                .id(1L)
                .name("Concerts")
                .build();
        Category category2 = Category.builder()
                .id(2L)
                .name("Exhibitions")
                .build();

        Mockito.when(categoryRepository.findAll(Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(category1, category2)));

        List<CategoryDto> result = categoryPublicService.getAll(20, 10);

        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        Mockito.verify(categoryRepository).findAll(pageRequestCaptor.capture());

        PageRequest pageRequest = pageRequestCaptor.getValue();
        assertThat(pageRequest.getPageNumber(), equalTo(2));
        assertThat(pageRequest.getPageSize(), equalTo(10));

        assertThat(result, hasSize(2));
        assertThat(result.stream()
                .map(CategoryDto::getId)
                .toList(), contains(category1.getId(), category2.getId()));
        assertThat(result.stream()
                .map(CategoryDto::getName)
                .toList(), contains(category1.getName(), category2.getName()));
    }
}
