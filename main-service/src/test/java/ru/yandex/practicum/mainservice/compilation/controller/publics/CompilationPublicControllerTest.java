package ru.yandex.practicum.mainservice.compilation.controller.publics;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mainservice.EventTestData;
import ru.yandex.practicum.mainservice.category.dto.CategoryMapper;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.compilation.dto.CompilationDto;
import ru.yandex.practicum.mainservice.compilation.service.publics.CompilationPublicService;
import ru.yandex.practicum.mainservice.event.dto.EventShortDto;
import ru.yandex.practicum.mainservice.user.dto.UserMapper;
import ru.yandex.practicum.mainservice.user.model.User;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(CompilationPublicController.class)
class CompilationPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompilationPublicService compilationPublicService;

    @Test
    void shouldGetAllCompilations() throws Exception {
        User user = EventTestData.makeUser(1L);
        Category category = EventTestData.makeCategory(1L);
        EventShortDto eventShortDto = EventShortDto.builder()
                .id(1L)
                .initiator(UserMapper.toUserDto(user))
                .category(CategoryMapper.toCategoryDto(category))
                .title("Concert")
                .annotation("annotation annotation annotation")
                .description("description description description")
                .eventDate(EventTestData.FIXED_TIME.plusDays(1).format(EventTestData.FORMATTER))
                .paid(false)
                .confirmedRequests(2L)
                .views(5L)
                .build();


        CompilationDto result = CompilationDto.builder()
                .id(1L)
                .pinned(false)
                .title("Title")
                .events(Set.of(eventShortDto))
                .build();

        Mockito.when(compilationPublicService.getCompilations(Mockito.anyBoolean(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(result));

        mockMvc.perform(get("/compilations")
                        .param("pinned", "false")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(result.getId()))
                .andExpect(jsonPath("$[0].title").value(result.getTitle()))
                .andExpect(jsonPath("$[0].pinned").value(result.getPinned()))
                .andExpect(jsonPath("$[0].events", hasSize(1)))
                .andExpect(jsonPath("$[0].events[0].id").value(eventShortDto.getId()));

        Mockito.verify(compilationPublicService)
                .getCompilations(false, 0, 10);

    }

    @Test
    void shouldGetAllCompilationsWithDefaultParams() throws Exception {
        Mockito.when(compilationPublicService.getCompilations(
                        Mockito.isNull(),
                        Mockito.eq(0),
                        Mockito.eq(10)))
                .thenReturn(List.of());

        mockMvc.perform(get("/compilations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        Mockito.verify(compilationPublicService)
                .getCompilations(null, 0, 10);
    }

    @Test
    void shouldGetCompilation() throws Exception {
        User user = EventTestData.makeUser(1L);
        Category category = EventTestData.makeCategory(1L);
        EventShortDto eventShortDto = EventShortDto.builder()
                .id(1L)
                .initiator(UserMapper.toUserDto(user))
                .category(CategoryMapper.toCategoryDto(category))
                .title("Concert")
                .annotation("annotation annotation annotation")
                .description("description description description")
                .eventDate(EventTestData.FIXED_TIME.plusDays(1).format(EventTestData.FORMATTER))
                .paid(false)
                .confirmedRequests(2L)
                .views(5L)
                .build();


        CompilationDto result = CompilationDto.builder()
                .id(1L)
                .pinned(false)
                .title("Title")
                .events(Set.of(eventShortDto))
                .build();

        Mockito.when(compilationPublicService.getCompilation(Mockito.anyLong()))
                .thenReturn(result);

        mockMvc.perform(get("/compilations/{compilationId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(result.getId()))
                .andExpect(jsonPath("$.title").value(result.getTitle()))
                .andExpect(jsonPath("$.pinned").value(result.getPinned()))
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].id").value(eventShortDto.getId()));

        Mockito.verify(compilationPublicService)
                .getCompilation(1L);

    }
}