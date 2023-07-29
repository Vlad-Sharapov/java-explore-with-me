package ru.yandex.practicum.statservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.statdto.HitDto;
import ru.yandex.practicum.statdto.StatsDto;
import ru.yandex.practicum.statservice.service.StatService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@Slf4j
@RequiredArgsConstructor
public class StatsController {

    private final StatService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public HitDto hit(@Valid @RequestBody HitDto hitDto) {
        log.info("Add new hit");
        return service.saveHit(hitDto);

    }

    @GetMapping("/stats")
    public List<StatsDto> getStat(@RequestParam LocalDateTime start,
                                  @RequestParam LocalDateTime end,
                                  @RequestParam(required = false) List<String> uris,
                                  @RequestParam(defaultValue = "false") Boolean unique) {

        List<StatsDto> stats = service.getStats(start, end, uris, unique);
        return stats;
    }
}
