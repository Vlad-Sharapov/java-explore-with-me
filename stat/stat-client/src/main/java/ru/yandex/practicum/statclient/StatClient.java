package ru.yandex.practicum.statclient;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;
import ru.yandex.practicum.statdto.HitDto;
import ru.yandex.practicum.statdto.StatsDto;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class StatClient {

    private final RestTemplate rest;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatClient(RestTemplate rest) {
        this.rest = rest;
    }


    public ResponseEntity<HitDto> addHit(String host, HitDto hitDto) {
        HttpEntity<HitDto> requestEntity = new HttpEntity<>(hitDto, defaultHeaders());

        return rest.exchange(host + "/hit",
                HttpMethod.POST, requestEntity, HitDto.class);
    }


    public ResponseEntity<List<StatsDto>> getStat(String host, LocalDateTime start, LocalDateTime end) {
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(host)
                .path("/stats")
                .queryParam("start", "{start}")
                .queryParam("end", "{end}")
                .encode()
                .toUriString();
        Map<String, Object> parameters = Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter)
        );

        return makeAndSendGetStatRequest(urlTemplate, parameters);
    }

    public ResponseEntity<List<StatsDto>> getStat(String host, LocalDateTime start, LocalDateTime end, String unique) {
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(host)
                .path("/stats")
                .queryParam("start", "{start}")
                .queryParam("end", "{end}")
                .queryParam("unique", "{unique}")
                .encode()
                .toUriString();
        Map<String, Object> parameters = Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter),
                "unique", unique
        );

        return makeAndSendGetStatRequest(urlTemplate, parameters);
    }

    public ResponseEntity<List<StatsDto>> getStat(String host, LocalDateTime start, LocalDateTime end, List<String> uris) {
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(host)
                .path("/stats")
                .queryParam("start", "{start}")
                .queryParam("end", "{end}")
                .queryParam("uris", "{uris}")
                .queryParam("unique", "{unique}")
                .encode()
                .toUriString();
        Map<String, Object> parameters = Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter),
                "uris", uris
        );

        return makeAndSendGetStatRequest(urlTemplate, parameters);
    }

    public ResponseEntity<List<StatsDto>> getStat(String host, LocalDateTime start, LocalDateTime end, List<String> uris, String unique) {
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(host)
                .path("/stats")
                .queryParam("start", "{start}")
                .queryParam("end", "{end}")
                .queryParam("uris", "{uris}")
                .queryParam("unique", "{unique}")
                .encode()
                .toUriString();
        Map<String, Object> parameters = Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter),
                "uris", uris,
                "unique", unique
        );

        return makeAndSendGetStatRequest(urlTemplate, parameters);
    }

    private ResponseEntity<List<StatsDto>> makeAndSendGetStatRequest(String urlTemplate, Map<String, Object> parameters) {
        HttpEntity<List<StatsDto>> requestEntity = new HttpEntity<>(defaultHeaders());

        return rest.exchange(urlTemplate, HttpMethod.GET, requestEntity,
                new ParameterizedTypeReference<>() {}, parameters);
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
