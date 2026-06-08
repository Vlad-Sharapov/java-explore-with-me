package ru.yandex.practicum.statservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.statdto.HitDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StatsServerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSaveHitAndReturnStats() throws Exception {
        HitDto hitDto = HitDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("1.1.1.1")
                .timestamp("2026-09-12 14:00:00")
                .build();

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hitDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uri").value("/events/1"));

        mockMvc.perform(get("/stats")
                        .param("start", "2026-09-12T13:00:00")
                        .param("end", "2026-09-12T15:00:00")
                        .param("uris", "/events/1")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("ewm-main-service"))
                .andExpect(jsonPath("$[0].uri").value("/events/1"))
                .andExpect(jsonPath("$[0].hits").value(1));
    }
}