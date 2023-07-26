package ru.yandex.practicum.statservice.dto;

import ru.yandex.practicum.statdto.HitDto;
import ru.yandex.practicum.statdto.StatsDto;
import ru.yandex.practicum.statservice.model.Hit;
import ru.yandex.practicum.statservice.model.Stats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class StatMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static HitDto toHitDto(Hit hit) {
        String timestamp = hit.getTime().format(formatter);

        return HitDto.builder()
                .id(hit.getId())
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(timestamp)
                .build();

    }

    public static Hit toHit(HitDto hitDto) {
        LocalDateTime timestamp = LocalDateTime.parse(hitDto.getTimestamp(),
                formatter);

        return Hit.builder()
                .id(hitDto.getId())
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .time(timestamp)
                .build();

    }

    public static StatsDto toStatsDto(Stats stats) {

        return StatsDto.builder()
                .app(stats.getApp())
                .uri(stats.getUri())
                .hits(stats.getHits())
                .build();

    }

    public static Stats toStats(StatsDto statsDto) {

        return Stats.builder()
                .app(statsDto.getApp())
                .uri(statsDto.getUri())
                .hits(statsDto.getHits())
                .build();

    }

    public static List<StatsDto> toStatsDto(Collection<Stats> items) {
        return items.stream()
                .map(StatMapper::toStatsDto)
                .collect(Collectors.toList());
    }
}
