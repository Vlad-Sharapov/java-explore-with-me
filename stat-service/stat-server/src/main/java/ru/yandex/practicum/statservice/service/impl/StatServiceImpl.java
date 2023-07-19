package ru.yandex.practicum.statservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.statdto.HitDto;
import ru.yandex.practicum.statdto.StatsDto;
import ru.yandex.practicum.statservice.dto.StatMapper;
import ru.yandex.practicum.statservice.exception.BadRequestException;
import ru.yandex.practicum.statservice.model.Hit;
import ru.yandex.practicum.statservice.repository.StatRepository;
import ru.yandex.practicum.statservice.service.StatService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final StatRepository repository;

    @Override
    @Transactional
    public HitDto saveHit(HitDto hitDto) {
        Hit hit = StatMapper.toHit(hitDto);
        Hit saveHit = repository.save(hit);
        log.info("Add new hit: " + saveHit);
        return StatMapper.toHitDto(saveHit);
    }

    @Override
    public List<StatsDto> getStats(LocalDateTime start,
                                   LocalDateTime end,
                                   List<String> uris,
                                   Boolean unique) {

        checkDateTime(start, end);

        if (uris == null) {
            if (unique) {
                return StatMapper.toStatsDto(repository.getAllUniqueHits(start, end, JpaSort.unsafe(Sort.Direction.DESC, "count(h.uri)")));
            } else
                return StatMapper.toStatsDto(repository.getAllHits(start, end, JpaSort.unsafe(Sort.Direction.DESC, "count(h.uri)")));
        } else {
            if (unique) {
                return StatMapper.toStatsDto(repository.getUniqueHitsByUris(uris, start, end, JpaSort.unsafe(Sort.Direction.DESC, "count(h.uri)")));
            } else {
                return StatMapper.toStatsDto(repository.getHitsByUris(uris, start, end, JpaSort.unsafe(Sort.Direction.DESC, "count(h.uri)")));
            }
        }


    }

    private void checkDateTime(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Start time is after than end time");
        }
    }
}
