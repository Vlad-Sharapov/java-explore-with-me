package ru.yandex.practicum.statclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.statdto.HitDto;
import ru.yandex.practicum.statdto.StatParamDto;
import ru.yandex.practicum.statdto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class StatClientTest {

    private static final String HOST = "http://localhost:9090";
    StatClient statClient;
    ObjectMapper objectMapper;
    MockRestServiceServer mockServer;

    @BeforeEach
    public void beforeEach() {
        objectMapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();
        statClient = new StatClient(restTemplate);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void get() {
        StatsDto dto = StatsDto.builder().uri("/events/1")
                .app("ewm-main-service")
                .hits(1L)
                .build();

        String response = toJson(List.of(dto, dto.toBuilder().uri("/events/2").build()));
        mockServer.expect(requestTo(startsWith(HOST + "/stats")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Content-Type", "application/json"))
                .andExpect(queryParam("start", "2026-09-12%2013:00:00"))
                .andExpect(queryParam("end", "2026-09-12%2015:00:00"))
                .andExpect(queryParam("uris", "/events/1,/events/2"))
                .andExpect(queryParam("unique", "true"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));


        ResponseEntity<List<StatsDto>> stat = statClient.getStat(HOST, StatParamDto.builder()
                .start(LocalDateTime.of(2026, 9, 12, 13, 0, 0))
                .end(LocalDateTime.of(2026, 9, 12, 15, 0, 0))
                .uris(List.of("/events/1", "/events/2"))
                .unique(true)
                .build());
        List<StatsDto> statsDtos1 = stat.getBody();
        assertThat(statsDtos1, hasSize(2));
        assertThat(statsDtos1, everyItem(hasProperty("hits", equalTo(1L))));

        mockServer.verify();
    }

    @Test
    void post() {
        HitDto hitDto = HitDto.builder()
                .uri("/events")
                .app("ewm-main-service")
                .ip("1.1.1.1")
                .timestamp("2026-09-12 14:00:00")
                .build();
        String response = toJson(HitDto.builder()
                .id(1L)
                .app("ewm-main-service")
                .uri("/events")
                .ip("1.1.1.1")
                .timestamp("2026-09-12 14:00:00")
                .build());
        mockServer.expect(requestTo(HOST + "/hit"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Accept", "application/json")).andExpect(jsonPath("$.app").value("ewm-main-service"))
                .andExpect(jsonPath("$.uri").value("/events"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        ResponseEntity<HitDto> hitDtoResponseEntity = statClient.addHit(HOST, hitDto);
        HitDto saveHitDto = hitDtoResponseEntity.getBody();

        assertThat(saveHitDto, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("uri", equalTo(hitDto.getUri())),
                hasProperty("app", equalTo(hitDto.getApp())),
                hasProperty("ip", equalTo(hitDto.getIp())),
                hasProperty("timestamp", equalTo(hitDto.getTimestamp()))
        ));

        mockServer.verify();
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
