package ru.yandex.practicum.statservice.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.JpaSort;
import ru.yandex.practicum.statdto.HitDto;
import ru.yandex.practicum.statdto.StatsDto;
import ru.yandex.practicum.statservice.exception.BadRequestException;
import ru.yandex.practicum.statservice.model.Hit;
import ru.yandex.practicum.statservice.model.Stats;
import ru.yandex.practicum.statservice.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class StatServiceImplTest {

    private final LocalDateTime startTime = LocalDateTime
            .of(2026, 9, 12, 14, 0, 0);
    @Mock
    private StatRepository statRepository;
    @InjectMocks
    private StatServiceImpl statService;

    @Test
    void saveHit() {
        HitDto hitDto = HitDto.builder()
                .uri("/event")
                .ip("1.1.1.1")
                .timestamp("2026-09-12 14:00:00")
                .app("ewm")
                .build();
        Mockito.when(statRepository.save(Mockito.any(Hit.class))).thenReturn(Hit.builder().id(1L)
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .app(hitDto.getApp())
                .time(startTime)
                .build());

        HitDto result = statService.saveHit(hitDto);

        assertThat(result, equalTo(hitDto.toBuilder().id(1L).build()));

        ArgumentCaptor<Hit> captor = ArgumentCaptor.forClass(Hit.class);

        Mockito.verify(statRepository).save(captor.capture());

        Hit savedHit = captor.getValue();

        assertThat(savedHit.getUri(), equalTo(hitDto.getUri()));
        assertThat(savedHit.getIp(), equalTo(hitDto.getIp()));
        assertThat(savedHit.getApp(), equalTo(hitDto.getApp()));
        assertThat(savedHit.getTime(), equalTo(startTime));
    }

    @Test
    void shouldGetStatsWhenUseGetAllHits() {
        Stats stats = Stats.builder().uri("/event")
                .app("getAll")
                .hits(3L)
                .build();

        Mockito.when(statRepository.getAllHits(Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(JpaSort.class))).thenReturn(List.of(stats));

        List<StatsDto> stats1 = statService.getStats(startTime, startTime, null, false);

        Mockito.verify(statRepository, Mockito.times(1)).getAllHits(
                Mockito.eq(startTime),
                Mockito.eq(startTime),
                Mockito.any(JpaSort.class)
        );

        Mockito.verify(statRepository, Mockito.never()).getAllUniqueHits(
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
        );

        assertThat(stats1, hasSize(1));
        assertThat(stats1.get(0).getApp(), equalTo("getAll"));
        assertThat(stats1.get(0).getUri(), equalTo("/event"));
        assertThat(stats1.get(0).getHits(), equalTo(3L));
    }

    @Test
    void shouldGetStatsWhenUseGetAllUniqueHits() {
        Stats stats = Stats.builder().uri("/event")
                .app("getAll")
                .hits(3L)
                .build();


        Mockito.when(statRepository.getAllUniqueHits(Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(JpaSort.class))).thenReturn(List.of(stats));

        List<StatsDto> stats1 = statService.getStats(startTime, startTime, null, true);

        Mockito.verify(statRepository, Mockito.times(1)).getAllUniqueHits(
                Mockito.eq(startTime),
                Mockito.eq(startTime),
                Mockito.any(JpaSort.class)
        );

        Mockito.verify(statRepository, Mockito.never()).getAllHits(
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
        );

        assertThat(stats1, hasSize(1));
        assertThat(stats1.get(0).getApp(), equalTo("getAll"));
        assertThat(stats1.get(0).getUri(), equalTo("/event"));
        assertThat(stats1.get(0).getHits(), equalTo(3L));
    }

    @Test
    void shouldGetStatsWhenUseGetUniqueHitsByUris() {
        Stats stats = Stats.builder().uri("/event")
                .app("getAll")
                .hits(3L)
                .build();


        Mockito.when(statRepository.getUniqueHitsByUris(Mockito.anyList(), Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(JpaSort.class))).thenReturn(List.of(stats));

        List<StatsDto> statsResult = statService.getStats(startTime, startTime, List.of("/event"), true);

        Mockito.verify(statRepository, Mockito.times(1)).getUniqueHitsByUris(
                Mockito.eq(List.of("/event")),
                Mockito.eq(startTime),
                Mockito.eq(startTime),
                Mockito.any(JpaSort.class)
        );

        Mockito.verify(statRepository, Mockito.never()).getHitsByUris(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
        );

        assertThat(statsResult, hasSize(1));
        assertThat(statsResult.get(0).getApp(), equalTo("getAll"));
        assertThat(statsResult.get(0).getUri(), equalTo("/event"));
        assertThat(statsResult.get(0).getHits(), equalTo(3L));
    }

    @Test
    void shouldGetStatsWhenUseGetAllHitsByUris() {
        Stats stats = Stats.builder().uri("/event")
                .app("getAll")
                .hits(3L)
                .build();


        Mockito.when(statRepository.getHitsByUris(Mockito.anyList(), Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(JpaSort.class))).thenReturn(List.of(stats));

        List<StatsDto> statsResult = statService.getStats(startTime, startTime, List.of("/event"), false);

        Mockito.verify(statRepository, Mockito.times(1)).getHitsByUris(
                Mockito.eq(List.of("/event")),
                Mockito.eq(startTime),
                Mockito.eq(startTime),
                Mockito.any(JpaSort.class)
        );

        Mockito.verify(statRepository, Mockito.never()).getUniqueHitsByUris(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
        );

        assertThat(statsResult, hasSize(1));
        assertThat(statsResult.get(0).getApp(), equalTo("getAll"));
        assertThat(statsResult.get(0).getUri(), equalTo("/event"));
        assertThat(statsResult.get(0).getHits(), equalTo(3L));
    }

    @Test
    void shouldGetStatsWhenUrisIsEmpty() {
        Stats stats = Stats.builder().uri("/event")
                .app("getAll")
                .hits(3L)
                .build();

        Mockito.when(statRepository.getAllHits(Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(JpaSort.class))).thenReturn(List.of(stats));

        List<StatsDto> statsResult = statService.getStats(startTime, startTime, List.of(), false);

        Mockito.verify(statRepository, Mockito.times(1)).getAllHits(
                Mockito.eq(startTime),
                Mockito.eq(startTime),
                Mockito.any(JpaSort.class)
        );

        Mockito.verify(statRepository, Mockito.never()).getHitsByUris(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
        );

        assertThat(statsResult, hasSize(1));
        assertThat(statsResult.get(0).getApp(), equalTo("getAll"));
        assertThat(statsResult.get(0).getUri(), equalTo("/event"));
        assertThat(statsResult.get(0).getHits(), equalTo(3L));
    }


    @Test
    void shouldThrowExceptionWhenGetStatsWithIncorrectTimestamp() {

        BadRequestException e = assertThrows(BadRequestException.class,
                () -> statService.getStats(startTime, startTime.minusDays(1), List.of("/event"), false));
        assertThat(e.getMessage(), containsString("Start time is after than end time"));

    }

}
