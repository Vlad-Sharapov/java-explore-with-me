package ru.yandex.practicum.mainservice.compilation;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.mainservice.TestUtils;
import ru.yandex.practicum.mainservice.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.mainservice.support.IntegrationTestSupport;

import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CompilationIntegrationTest extends IntegrationTestSupport {

    @Test
    void shouldCreateCompilationAndReturnItById() throws Exception {
        Long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long categoryId = createCategory("category");
        Long firstEventId = createEvent(initiatorId, categoryId, true, 10);
        Long secondEventId = createEvent(initiatorId, categoryId, true, 10);
        publishEvent(firstEventId);
        publishEvent(secondEventId);

        Long compilationId = createCompilation(
                "Popular events",
                true,
                Set.of(firstEventId, secondEventId)
        );

        mockMvc.perform(get("/compilations/{compilationId}", compilationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(compilationId))
                .andExpect(jsonPath("$.title").value("Popular events"))
                .andExpect(jsonPath("$.pinned").value(true))
                .andExpect(jsonPath("$.events", hasSize(2)))
                .andExpect(jsonPath("$.events[*].id", containsInAnyOrder(
                        firstEventId.intValue(),
                        secondEventId.intValue()
                )));
    }

    @Test
    void shouldFilterCompilationsByPinned() throws Exception {
        Long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long categoryId = createCategory("category");
        Long eventId = createEvent(initiatorId, categoryId, true, 10);
        publishEvent(eventId);

        Long pinnedCompilationId = createCompilation(
                "Pinned compilation",
                true,
                Set.of(eventId)
        );
        createCompilation("Regular compilation", false, Set.of(eventId));

        mockMvc.perform(get("/compilations")
                        .param("pinned", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(pinnedCompilationId))
                .andExpect(jsonPath("$[0].title").value("Pinned compilation"))
                .andExpect(jsonPath("$[0].pinned").value(true));
    }

    @Test
    void shouldUpdateCompilationAndPersistChanges() throws Exception {
        Long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long categoryId = createCategory("category");
        Long firstEventId = createEvent(initiatorId, categoryId, true, 10);
        Long secondEventId = createEvent(initiatorId, categoryId, true, 10);
        publishEvent(firstEventId);
        publishEvent(secondEventId);

        Long compilationId = createCompilation(
                "Initial title",
                false,
                Set.of(firstEventId)
        );

        NewCompilationDto update = NewCompilationDto.builder()
                .title("Updated title")
                .pinned(true)
                .events(Set.of(secondEventId))
                .build();

        mockMvc.perform(patch("/admin/compilations/{compilationId}", compilationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(objectMapper, update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(compilationId))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.pinned").value(true))
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].id").value(secondEventId));

        mockMvc.perform(get("/compilations/{compilationId}", compilationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.pinned").value(true))
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].id").value(secondEventId));
    }

    @Test
    void shouldDeleteCompilation() throws Exception {
        Long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long categoryId = createCategory("category");
        Long eventId = createEvent(initiatorId, categoryId, true, 10);
        publishEvent(eventId);

        Long compilationId = createCompilation(
                "Compilation to delete",
                false,
                Set.of(eventId)
        );

        mockMvc.perform(delete("/admin/compilations/{compilationId}", compilationId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/compilations/{compilationId}", compilationId))
                .andExpect(status().isNotFound());
    }

    private Long createCompilation(String title,
                                   boolean pinned,
                                   Set<Long> eventIds) throws Exception {
        NewCompilationDto compilation = NewCompilationDto.builder()
                .title(title)
                .pinned(pinned)
                .events(eventIds)
                .build();

        MvcResult result = mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(objectMapper, compilation)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.pinned").value(pinned))
                .andExpect(jsonPath("$.events", hasSize(eventIds.size())))
                .andReturn();

        return TestUtils.extractId(objectMapper, result);
    }
}
