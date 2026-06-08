package ru.yandex.practicum.mainservice.category.controller.publics;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mainservice.category.dto.CategoryDto;
import ru.yandex.practicum.mainservice.category.service.publics.CategoryPublicService;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(CategoryPublicController.class)
class CategoryPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryPublicService categoryPublicService;

    @Test
    void shouldGetAllCategories() throws Exception {

        CategoryDto category1 = CategoryDto.builder()
                .id(1L)
                .name("Concerts")
                .build();
        CategoryDto category2 = CategoryDto.builder()
                .id(2L)
                .name("Exhibitions")
                .build();

        Mockito.when(categoryPublicService.getAll(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(category1, category2));

        mockMvc.perform(get("/categories")
                        .param("from", "20")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(category1.getId()))
                .andExpect(jsonPath("$[0].name").value(category1.getName()))
                .andExpect(jsonPath("$[1].id").value(category2.getId()))
                .andExpect(jsonPath("$[1].name").value(category2.getName()));

        Mockito.verify(categoryPublicService).getAll(20, 10);
    }

    @Test
    void shouldGetAllCategoriesWithDefaultParams() throws Exception {
        Mockito.when(categoryPublicService.getAll(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(categoryPublicService).getAll(0, 10);
    }

    @Test
    void shouldGetCategoryById() throws Exception {
        CategoryDto categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Concerts")
                .build();

        Mockito.when(categoryPublicService.get(Mockito.anyLong()))
                .thenReturn(categoryDto);

        mockMvc.perform(get("/categories/{catId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryDto.getName()));

        Mockito.verify(categoryPublicService).get(1L);
    }

    @Test
    void shouldThrowNotFoundWhenCategoryDoesNotExist() throws Exception {
        Mockito.when(categoryPublicService.get(Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException("Category with id - 1 not found"));

        mockMvc.perform(get("/categories/{catId}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404 NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("The required object was not found."))
                .andExpect(jsonPath("$.message").value("Category with id - 1 not found"))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(categoryPublicService).get(1L);
    }
}
