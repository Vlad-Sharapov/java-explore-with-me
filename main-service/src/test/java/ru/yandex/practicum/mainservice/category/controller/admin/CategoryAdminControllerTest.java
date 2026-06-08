package ru.yandex.practicum.mainservice.category.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mainservice.TestUtils;
import ru.yandex.practicum.mainservice.category.dto.CategoryDto;
import ru.yandex.practicum.mainservice.category.service.admin.CategoryAdminService;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(CategoryAdminController.class)
class CategoryAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryAdminService categoryAdminService;


    @Test
    void shouldAddNewCategory() throws Exception {

        CategoryDto categoryDto = CategoryDto.builder()
                .id(1L)
                .name("test").build();

        Mockito.when(categoryAdminService.addCategory(Mockito.any(CategoryDto.class)))
                .thenReturn(categoryDto);

        mockMvc.perform(post("/admin/categories")
                        .content(TestUtils.asJsonString(objectMapper, categoryDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("test"));

        Mockito.verify(categoryAdminService, Mockito.times(1))
                .addCategory(Mockito.any(CategoryDto.class));

    }

    @Test
    void shouldThrowWhenCategoryNameIsNull() throws Exception {
        CategoryDto categoryDto = CategoryDto.builder()
                .id(1L)
                .name(null).build();

        mockMvc.perform(post("/admin/categories")
                        .content(TestUtils.asJsonString(objectMapper, categoryDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.message").value("Field: name. Error: must not be blank."))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(categoryAdminService, Mockito.never())
                .addCategory(Mockito.any(CategoryDto.class));
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        CategoryDto updateCategoryDto = CategoryDto.builder()
                .name("updated test").build();

        CategoryDto categoryDto = CategoryDto.builder()
                .id(1L)
                .name("updated test").build();

        Mockito.when(categoryAdminService.update(Mockito.anyLong(), Mockito.any(CategoryDto.class)))
                .thenReturn(categoryDto);

        mockMvc.perform(patch("/admin/categories/{catId}", 1L)
                        .content(TestUtils.asJsonString(objectMapper, updateCategoryDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("updated test"));

        Mockito.verify(categoryAdminService, Mockito.times(1))
                .update(Mockito.anyLong(), Mockito.any(CategoryDto.class));
    }

    @Test
    void shouldThrowWhenTitleTooLarge() throws Exception {

        CategoryDto categoryDto = CategoryDto.builder()
                .id(1L)
                .name("large test large test large test large test large test large test large test large test").build();

        mockMvc.perform(patch("/admin/categories/{catId}", 1L)
                        .content(TestUtils.asJsonString(objectMapper, categoryDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.message").value("Field: name. Error: Must not be less than 1 characters and more than 50 characters."))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(categoryAdminService, Mockito.never())
                .update(Mockito.anyLong(), Mockito.any(CategoryDto.class));
    }

    @Test
    void shouldThrowNotFoundWhenUpdateCategoryDoesNotExist() throws Exception {
        CategoryDto categoryDto = CategoryDto.builder()
                .name("updated test")
                .build();

        Mockito.when(categoryAdminService.update(Mockito.anyLong(), Mockito.any(CategoryDto.class)))
                .thenThrow(new EntityNotFoundException("Category with id=1 was not found"));

        mockMvc.perform(patch("/admin/categories/{catId}", 1L)
                        .content(TestUtils.asJsonString(objectMapper, categoryDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404 NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("The required object was not found."))
                .andExpect(jsonPath("$.message").value("Category with id=1 was not found"))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(categoryAdminService)
                .update(Mockito.eq(1L), Mockito.any(CategoryDto.class));
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        mockMvc.perform(delete("/admin/categories/{catId}", 1L))
                .andExpect(status().isNoContent());

        Mockito.verify(categoryAdminService, Mockito.times(1)).deleteCategory(1L);
    }

    @Test
    void shouldThrowNotFoundWhenDeleteCategoryDoesNotExist() throws Exception {
        Mockito.doThrow(new EntityNotFoundException("Category with id=1 was not found"))
                .when(categoryAdminService)
                .deleteCategory(Mockito.anyLong());

        mockMvc.perform(delete("/admin/categories/{catId}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404 NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("The required object was not found."))
                .andExpect(jsonPath("$.message").value(containsString("Category with id=1")))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(categoryAdminService).deleteCategory(1L);
    }
}
