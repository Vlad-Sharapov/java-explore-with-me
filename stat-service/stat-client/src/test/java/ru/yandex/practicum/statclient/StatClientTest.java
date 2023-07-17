package ru.yandex.practicum.statclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.statdto.HitDto;
import ru.yandex.practicum.statdto.StatParamDto;
import ru.yandex.practicum.statdto.StatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class StatClientTest {

    StatClient statClient;

    private final String statServerUrl = "http://localhost:9090";

    @BeforeEach
    public void beforeEach() {
        statClient = new StatClient(new RestTemplate());
    }

    @Test
    void get() {

        ResponseEntity<List<StatsDto>> stat = statClient.getStat(statServerUrl, StatParamDto.builder()
                .start(LocalDateTime.now().minusHours(8))
                .end(LocalDateTime.now().plusHours(1))
                .build());
        List<StatsDto> statsDtos1 = stat.getBody();
        System.out.println(statsDtos1);
        assertThat(statsDtos1, hasSize(1));

    }

    @Test
    void post() {
        HitDto hitDto = HitDto.builder()
                .uri("/events")
                .app("ewm-main-service")
                .ip("121.0.0.1")
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).build();
        ResponseEntity<HitDto> hitDtoResponseEntity = statClient.addHit(statServerUrl, hitDto);
        HitDto saveHitDto = hitDtoResponseEntity.getBody();

        assertThat(saveHitDto, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("uri", equalTo(hitDto.getUri())),
                hasProperty("app", equalTo(hitDto.getApp())),
                hasProperty("ip", equalTo(hitDto.getIp())),
                hasProperty("timestamp", equalTo(hitDto.getTimestamp()))
        ));
    }
}