package ru.yandex.practicum.mainservice.event.stateclient;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.exceptions.BadRequestException;
import ru.yandex.practicum.statclient.StatClient;
import ru.yandex.practicum.statdto.HitDto;
import ru.yandex.practicum.statdto.StatParamDto;
import ru.yandex.practicum.statdto.StatsDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientHandler {

    private final StatClient statClient;

    @Value("${state-server.url}")
    private String host;

    @Value("${main-server.app-name}")
    private String APP;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public List<StatsDto> getStatsForEvent(Event event, LocalDateTime start, LocalDateTime end) {
        List<String> uris = List.of("/events/" + event.getId());
        StatParamDto statParamDto = StatParamDto.builder()
                .start(start)
                .end(end)
                .uris(uris)
                .unique(true)
                .build();
        return getStats(statParamDto);
    }

    public  List<StatsDto> getStatsForEvents(Collection<Event> events, LocalDateTime start, LocalDateTime end) {
        List<String> uris = events.stream().map(event -> "/events/" + event.getId()).collect(Collectors.toList());
        StatParamDto statParamDto = StatParamDto.builder()
                .start(start)
                .end(end)
                .uris(uris)
                .unique(true)
                .build();
        return getStats(statParamDto);
    }

    public List<StatsDto> getStats(StatParamDto statParamDto) {
        ResponseEntity<List<StatsDto>> stat = statClient.getStat(host, statParamDto);
        if (stat.getStatusCode() == HttpStatus.BAD_REQUEST) {
            throw new BadRequestException("Bad request");
        }
        if (stat.getStatusCode() != HttpStatus.OK) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Bad request to the statistics service");
        }
        return stat.getBody();
    }

    public void addHit(HttpServletRequest request) {
        statClient.addHit(host, HitDto.builder()
                .app(APP)
                .timestamp(LocalDateTime.now().format(formatter))
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .build());
    }
}
