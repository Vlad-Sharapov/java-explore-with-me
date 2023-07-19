package ru.yandex.practicum.statservice.repository;

import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.statservice.model.Hit;
import ru.yandex.practicum.statservice.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository extends JpaRepository<Hit, Long> {

    @Query("select new ru.yandex.practicum.statservice.model.Stats(h.app, h.uri, count(h.id)) " +
            "from Hit as h where h.uri in ?1 and (h.time between ?2 and ?3) " +
            "group by h.uri, h.app")
    List<Stats> getHitsByUris(List<String> uris, LocalDateTime start, LocalDateTime end, JpaSort sort);

    @Query("select new ru.yandex.practicum.statservice.model.Stats(h.app, h.uri, count(distinct h.ip)) " +
            "from Hit as h where h.uri in ?1 and h.time between ?2 and ?3 " +
            "group by h.uri, h.app")
    List<Stats> getUniqueHitsByUris(List<String> uris, LocalDateTime start, LocalDateTime end, JpaSort sort);

    @Query("select new ru.yandex.practicum.statservice.model.Stats(h.app, h.uri, count(h.id)) " +
            "from Hit as h where h.time between ?1 and ?2 " +
            "group by h.uri, h.app")
    List<Stats> getAllHits(LocalDateTime start, LocalDateTime end, JpaSort sort);

    @Query("select new ru.yandex.practicum.statservice.model.Stats(h.app, h.uri, count(distinct h.ip)) " +
            "from Hit as h where h.time between ?1 and ?2 " +
            "group by h.uri, h.app")
    List<Stats> getAllUniqueHits(LocalDateTime start, LocalDateTime end, JpaSort sort);
}
