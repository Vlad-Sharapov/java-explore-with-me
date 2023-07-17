package ru.yandex.practicum.statservice.service;

import ru.yandex.practicum.statdto.HitDto;
import ru.yandex.practicum.statdto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {

    HitDto saveHit(HitDto hitDto);

    List<StatsDto> getStats(LocalDateTime start,
                            LocalDateTime end,
                            List<String> uris,
                            Boolean unique);

}
