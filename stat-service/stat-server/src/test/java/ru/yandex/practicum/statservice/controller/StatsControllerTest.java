package ru.yandex.practicum.statservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.statdto.HitDto;
import ru.yandex.practicum.statservice.exception.BadRequestException;
import ru.yandex.practicum.statservice.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = StatsController.class)
class StatsControllerTest {

    HitDto hitDto = HitDto.builder()
            .uri("/event")
            .app("ewm")
            .ip("1.1.1.1")
            .timestamp("2026-09-12 14:00:00").build();
    HitDto notValidHitDto = HitDto.builder()
            .uri("/event")
            .ip("1.1.1.1")
            .timestamp("2026-09-12 14:00:00").build();
    @MockitoBean
    private StatService statService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void hit() throws Exception {
        Mockito.when(statService.saveHit(Mockito.any(HitDto.class)))
                .thenReturn(hitDto);

        mockMvc.perform(post("/hit")
                        .content(asJsonString(hitDto))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uri").value("/event"))
                .andExpect(jsonPath("$.timestamp").value("2026-09-12 14:00:00"))
                .andExpect(jsonPath("$.app").value("ewm"));

        Mockito.verify(statService).saveHit(Mockito.any(HitDto.class));
    }

    @Test
    void shouldReturn400WhenAddNotValidHit() throws Exception {

        mockMvc.perform(post("/hit")
                        .content(asJsonString(notValidHitDto))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verify(statService, Mockito.never()).saveHit(Mockito.any());
    }

    @Test
    void shouldGetStatsWithoutUris() throws Exception {
        Mockito.when(statService.getStats(
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.isNull(),
                        Mockito.eq(false)))
                .thenReturn(List.of());

        mockMvc.perform(get("/stats")
                        .param("start", "2026-09-12T14:00:00")
                        .param("end", "2026-09-12T15:00:00"))
                .andExpect(status().isOk());

        Mockito.verify(statService, Mockito.times(1)).getStats(
                Mockito.eq(LocalDateTime.of(2026, 9, 12, 14, 0, 0)),
                Mockito.eq(LocalDateTime.of(2026, 9, 12, 15, 0, 0)),
                Mockito.isNull(),
                Mockito.eq(false)
        );
    }

    @Test
    void shouldGetStatsWithoutUrisAndWithUnique() throws Exception {
        Mockito.when(statService.getStats(
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.isNull(),
                        Mockito.eq(true)))
                .thenReturn(List.of());

        mockMvc.perform(get("/stats")
                        .param("start", "2026-09-12T14:00:00")
                        .param("end", "2026-09-12T15:00:00")
                        .param("unique", "true"))
                .andExpect(status().isOk());

        Mockito.verify(statService, Mockito.times(1)).getStats(
                Mockito.eq(LocalDateTime.of(2026, 9, 12, 14, 0, 0)),
                Mockito.eq(LocalDateTime.of(2026, 9, 12, 15, 0, 0)),
                Mockito.isNull(),
                Mockito.eq(true)
        );
    }

    @Test
    void shouldGetStatsWithUrisAndUnique() throws Exception {
        Mockito.when(statService.getStats(
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.eq(List.of("/event/1", "/event/2")),
                        Mockito.eq(true)))
                .thenReturn(List.of());

        mockMvc.perform(get("/stats")
                        .param("start", "2026-09-12T14:00:00")
                        .param("end", "2026-09-12T15:00:00")
                        .param("uris", "/event/1", "/event/2")
                        .param("unique", "true"))
                .andExpect(status().isOk());

        Mockito.verify(statService, Mockito.times(1)).getStats(
                Mockito.eq(LocalDateTime.of(2026, 9, 12, 14, 0, 0)),
                Mockito.eq(LocalDateTime.of(2026, 9, 12, 15, 0, 0)),
                Mockito.eq(List.of("/event/1", "/event/2")),
                Mockito.eq(true)
        );
    }

    @Test
    void shouldReturnBadRequestWhenStartIsAfterEnd() throws Exception {

        Mockito.when(statService.getStats(
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.isNull(),
                        Mockito.eq(false)))
                .thenThrow(new BadRequestException("Start time is after than end time"));

        mockMvc.perform(get("/stats")
                        .param("start", "2026-09-12T16:00:00")
                        .param("end", "2026-09-12T15:00:00"))
                .andExpect(status().isBadRequest());

    }


    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}