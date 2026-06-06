package ru.yandex.practicum.mainservice.category.service.admin.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mainservice.EventTestData;
import ru.yandex.practicum.mainservice.category.dto.CategoryDto;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.category.repository.CategoryRepository;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CategoryAdminServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryAdminServiceImpl categoryAdminService;

    @Test
    void shouldAddCategory() {
        CategoryDto categoryDto = CategoryDto.builder()
                .name("Test")
                .build();

        Mockito.when(categoryRepository.save(Mockito.any(Category.class))).thenAnswer(
                invocation -> {
                    Category category = invocation.getArgument(0);
                    category.setId(1L);
                    return category;
                }
        );

        CategoryDto result = categoryAdminService.addCategory(categoryDto);

        Mockito.verify(categoryRepository, Mockito.times(1)).save(Mockito.any(Category.class));

        assertThat(result.getId(), equalTo(1L));
        assertThat(result.getName(), equalTo("Test"));
    }

    @Test
    void shouldUpdateCategory() {
        Category category = EventTestData.makeCategory(1L, "test");

        Category updatedCategory = EventTestData.makeCategory(1L, "Updated");

        CategoryDto updatedCategoryDto = CategoryDto.builder()
                .name("Updated")
                .build();

        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(category));

        Mockito.when(categoryRepository.save(Mockito.any(Category.class)))
                .thenReturn(updatedCategory);

        CategoryDto result = categoryAdminService.update(updatedCategory.getId(), updatedCategoryDto);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);


        Mockito.verify(categoryRepository, Mockito.times(1))
                .save(captor.capture());

        Category capturedCategory = captor.getValue();

        assertThat(capturedCategory.getName(), equalTo("Updated"));

        assertThat(result.getId(), equalTo(1L));
        assertThat(result.getName(), equalTo("Updated"));

    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {

        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> categoryAdminService.update(111L, CategoryDto.builder().build()));

        Mockito.verify(categoryRepository, Mockito.never()).save(Mockito.any(Category.class));
    }

    @Test
    void shouldUpdateCategoryWithoutChangingNameWhenNameIsNull() {
        Category category = EventTestData.makeCategory(1L, "test");
        CategoryDto updatedCategoryDto = CategoryDto.builder()
                .name(null)
                .build();

        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(category));
        Mockito.when(categoryRepository.save(Mockito.any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoryDto result = categoryAdminService.update(category.getId(), updatedCategoryDto);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        Mockito.verify(categoryRepository).save(captor.capture());

        assertThat(captor.getValue().getName(), equalTo("test"));
        assertThat(result.getId(), equalTo(1L));
        assertThat(result.getName(), equalTo("test"));
    }

    @Test
    void shouldDeleteCategory() {
        Category category = EventTestData.makeCategory(1L, "test");

        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(category));

        categoryAdminService.deleteCategory(category.getId());

        Mockito.verify(categoryRepository).findById(category.getId());
        Mockito.verify(categoryRepository).delete(category);
    }

    @Test
    void shouldThrowExceptionWhenDeleteCategoryDoesNotExist() {
        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> categoryAdminService.deleteCategory(111L));

        Mockito.verify(categoryRepository, Mockito.never()).delete(Mockito.any(Category.class));
    }
}
