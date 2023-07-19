package ru.yandex.practicum.statclient;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.yandex.practicum.statdto.HitDto;
import ru.yandex.practicum.statdto.StatParamDto;
import ru.yandex.practicum.statdto.StatsDto;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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


    public ResponseEntity<List<StatsDto>> getStat(String host, StatParamDto parameters) {
        HttpEntity<List<StatsDto>> requestEntity = new HttpEntity<>(defaultHeaders());
        Map<String, Object> parametersMap = new HashMap<>();
        UriComponentsBuilder path = UriComponentsBuilder.fromHttpUrl(host).path("/stats")
                .queryParam("start", "{start}")
                .queryParam("end", "{end}");

        parametersMap.put("start", parameters.getStart().format(formatter));
        parametersMap.put("end", parameters.getEnd().format(formatter));

        if (parameters.hasUris()) {
            parametersMap.put("uris", parameters.getUris());
            path.queryParam("uris", "{uris}");
        }

        if (parameters.hasUnique()) {
            parametersMap.put("unique", parameters.getUnique());
            path.queryParam("uris", "{uris}");
        }

        String urlTemplate = path
                .encode()
                .toUriString();

        return rest.exchange(urlTemplate, HttpMethod.GET, requestEntity,
                new ParameterizedTypeReference<>() {
                }, parametersMap);
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
