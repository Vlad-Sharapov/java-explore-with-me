package ru.yandex.practicum.mainservice.compilation.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mainservice.EventTestData;
import ru.yandex.practicum.mainservice.TestUtils;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.compilation.dto.CompilationDto;
import ru.yandex.practicum.mainservice.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.mainservice.compilation.service.admin.impl.CompilationAdminServiceImpl;
import ru.yandex.practicum.mainservice.event.dto.EventMapper;
import ru.yandex.practicum.mainservice.event.dto.EventShortDto;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.user.model.User;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(CompilationAdminController.class)
class CompilationAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompilationAdminServiceImpl compilationService;

    @Test
    void add() throws Exception {
        User initiator = EventTestData.makeUser(1L);
        Category category = EventTestData.makeCategory(1L);
        Location location = EventTestData.makeLocation(1L);

        Event event = EventTestData.makeEvent(initiator, category, location)
                .build();

        EventShortDto eventShortDto = EventMapper.toEventShortDto(event, 0L);

        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .pinned(false)
                .title("title")
                .events(Set.of(1L))
                .build();

        CompilationDto compilationDto = CompilationDto.builder()
                .id(1L)
                .title("title")
                .events(Set.of(eventShortDto))
                .pinned(false)
                .build();


        Mockito.when(compilationService.add(Mockito.any(NewCompilationDto.class)))
                .thenReturn(compilationDto);

        mockMvc.perform(post("/admin/compilations")
                        .content(TestUtils.asJsonString(objectMapper, newCompilationDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.events[0].id").value(1));

    }

    @Test
    void shouldThrow400ExceptionWhenCompilationTitleNotFound() throws Exception {

        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .pinned(false)
                .title(null)
                .events(Set.of(1L))
                .build();


        mockMvc.perform(post("/admin/compilations")
                        .content(TestUtils.asJsonString(objectMapper, newCompilationDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.message").value("Field: title. Error: Must not be less than 1 character and more than 50 characters."))
                .andExpect(jsonPath("$.timestamp").exists());
        Mockito.verify(compilationService, Mockito.never()).add(Mockito.any(NewCompilationDto.class));
    }

    @Test
    void shouldThrow400ExceptionWhenTitleSizeTooLarge() throws Exception {
        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .pinned(false)
                .title("title too large title too large title too large title too large title too large title too large")
                .events(Set.of(1L))
                .build();


        mockMvc.perform(post("/admin/compilations")
                        .content(TestUtils.asJsonString(objectMapper, newCompilationDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.message").value("Field: title. Error: Must not be less than 1 character and more than 50 characters."))
                .andExpect(jsonPath("$.timestamp").exists());
        Mockito.verify(compilationService, Mockito.never()).add(Mockito.any(NewCompilationDto.class));

    }

    @Test
    void update() throws Exception {
        User initiator = EventTestData.makeUser(1L);
        Category category = EventTestData.makeCategory(1L);
        Location location = EventTestData.makeLocation(1L);

        Event event = EventTestData.makeEvent(initiator, category, location)
                .build();

        EventShortDto eventShortDto = EventMapper.toEventShortDto(event, 0L);

        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .pinned(false)
                .title("update title")
                .events(Set.of(1L))
                .build();

        CompilationDto compilationDto = CompilationDto.builder()
                .id(1L)
                .title("update title")
                .events(Set.of(eventShortDto))
                .pinned(false)
                .build();


        Mockito.when(compilationService.update(Mockito.anyLong(), Mockito.any(NewCompilationDto.class)))
                .thenReturn(compilationDto);

        mockMvc.perform(patch("/admin/compilations/{id}", compilationDto.getId())
                        .content(TestUtils.asJsonString(objectMapper, newCompilationDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("update title"))
                .andExpect(jsonPath("$.events[0].id").value(1));
    }


    @Test
    void shouldUpdateWhenTitleIsNull() throws Exception {
        User initiator = EventTestData.makeUser(1L);
        Category category = EventTestData.makeCategory(1L);
        Location location = EventTestData.makeLocation(1L);

        Event event = EventTestData.makeEvent(initiator, category, location)
                .build();

        EventShortDto eventShortDto = EventMapper.toEventShortDto(event, 0L);

        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .pinned(false)
                .title(null)
                .events(Set.of(1L))
                .build();

        CompilationDto compilationDto = CompilationDto.builder()
                .id(1L)
                .title("title")
                .events(Set.of(eventShortDto))
                .pinned(false)
                .build();


        Mockito.when(compilationService.update(Mockito.anyLong(), Mockito.any(NewCompilationDto.class)))
                .thenReturn(compilationDto);

        mockMvc.perform(patch("/admin/compilations/{id}", compilationDto.getId())
                        .content(TestUtils.asJsonString(objectMapper, newCompilationDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.events[0].id").value(1));
    }

    @Test
    void shouldThrow400WhenUpdateServiceNotFoundId() throws Exception {
        NewCompilationDto newCompilationDto = NewCompilationDto.builder()
                .pinned(false)
                .title("title")
                .events(Set.of(1L))
                .build();

        Mockito.when(compilationService.update(Mockito.anyLong(), Mockito.any(NewCompilationDto.class)))
                .thenThrow(new EntityNotFoundException("Compilation with id=1 was not found"));

        mockMvc.perform(patch("/admin/compilations/{id}", newCompilationDto.getId())
                        .content(TestUtils.asJsonString(objectMapper, newCompilationDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404 NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("The required object was not found."))
                .andExpect(jsonPath("$.message").value("Compilation with id=1 was not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldDeleteCompilation() throws Exception {

        mockMvc.perform(delete("/admin/compilations/{id}", 1L))
                .andExpect(status().isNoContent());

        Mockito.verify(compilationService, Mockito.times(1)).delete(1L);
    }
}