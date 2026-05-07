package ru.yandex.practicum.statservice.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.statservice.model.Hit;
import ru.yandex.practicum.statservice.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@DataJpaTest
class StatRepositoryTest {

    private final LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);
    private final Hit hit1 = Hit.builder().ip("192.168.1.1").uri("/event").app("ewm-main-service")
            .time(start).build();
    private final Hit hit2 = Hit.builder().ip("192.168.1.1").uri("/event").app("ewm-main-service")
            .time(start.plusSeconds(1)).build();
    private final Hit hit3 = Hit.builder().ip("192.168.1.2").uri("/requests").app("ewm-main-service")
            .time(start.plusSeconds(2)).build();
    private final Hit hit4 = Hit.builder().ip("192.168.1.3").uri("/more").app("ewm-main-service")
            .time(start.plusSeconds(3)).build();
    private final Hit hit5 = Hit.builder().ip("192.168.1.1").uri("/more").app("ewm-main-service")
            .time(start.plusSeconds(4)).build();
    private final Hit hit6 = Hit.builder().ip("192.168.1.4").uri("/more").app("ewm-main-service")
            .time(start.plusSeconds(5)).build();
    @Autowired
    private StatRepository statRepository;
    @Autowired
    private TestEntityManager em;

    @BeforeEach
    void setUp() {
        em.persist(hit1);
        em.persist(hit2);
        em.persist(hit3);
        em.persist(hit4);
        em.persist(hit5);
        em.persist(hit6);
        em.flush();
    }

    @Test
    void shouldReturnHitsFilteredByUris() {
        List<Stats> hitsByUris = statRepository.getHitsByUris(
                List.of(hit3.getUri(), hit4.getUri()),
                start.minusHours(1),
                start.plusHours(1),
                JpaSort.unsafe(Sort.Direction.DESC, "count(h.uri)"));

        assertThat(hitsByUris, hasSize(2));
        assertThat(findByUri(hitsByUris, "/more"), allOf(
                hasProperty("app", equalTo("ewm-main-service")),
                hasProperty("uri", equalTo("/more")),
                hasProperty("hits", equalTo(3L)
                )));
        assertThat(findByUri(hitsByUris, "/requests"), allOf(
                hasProperty("app", equalTo("ewm-main-service")),
                hasProperty("uri", equalTo("/requests")),
                hasProperty("hits", equalTo(1L)
                )));
    }

    @Test
    void shouldReturnUniqueHitsFilteredByUris() {
        List<Stats> uniqueHitsByUris = statRepository.getUniqueHitsByUris(
                List.of(hit2.getUri(), hit4.getUri()),
                start.minusHours(1),
                start.plusHours(1),
                JpaSort.unsafe(Sort.Direction.DESC, "count(h.uri)"));

        assertThat(uniqueHitsByUris, hasSize(2));
        assertThat(findByUri(uniqueHitsByUris, "/more"), allOf(
                hasProperty("app", equalTo("ewm-main-service")),
                hasProperty("uri", equalTo("/more")),
                hasProperty("hits", equalTo(3L)
                )));

        assertThat(findByUri(uniqueHitsByUris, "/event"), allOf(
                hasProperty("app", equalTo("ewm-main-service")),
                hasProperty("uri", equalTo("/event")),
                hasProperty("hits", equalTo(1L)
                )));
    }

    @Test
    void shouldReturnAllHitsGroupedByUri() {
        List<Stats> hitsByUris = statRepository.getAllHits(
                start.minusHours(1),
                start.plusHours(1),
                JpaSort.unsafe(Sort.Direction.DESC, "count(h.uri)"));

        assertThat(hitsByUris, hasSize(3));
        assertThat(findByUri(hitsByUris, "/more"), allOf(
                hasProperty("app", equalTo("ewm-main-service")),
                hasProperty("uri", equalTo("/more")),
                hasProperty("hits", equalTo(3L)

                )));
        assertThat(findByUri(hitsByUris, "/requests"), allOf(
                hasProperty("app", equalTo("ewm-main-service")),
                hasProperty("uri", equalTo("/requests")),
                hasProperty("hits", equalTo(1L)
                )));
        assertThat(findByUri(hitsByUris, "/event"), allOf(
                hasProperty("app", equalTo("ewm-main-service")),
                hasProperty("uri", equalTo("/event")),
                hasProperty("hits", equalTo(2L)
                )));
    }

    @Test
    void shouldReturnAllUniqueHitsGroupedByUri() {
        List<Stats> hitsByUris = statRepository.getAllUniqueHits(
                start.minusHours(1),
                start.plusHours(1),
                JpaSort.unsafe(Sort.Direction.DESC, "count(h.uri)"));

        assertThat(hitsByUris, hasSize(3));
        assertThat(findByUri(hitsByUris, "/more"), allOf(
                hasProperty("app", equalTo("ewm-main-service")),
                hasProperty("uri", equalTo("/more")),
                hasProperty("hits", equalTo(3L)

                )));
        assertThat(findByUri(hitsByUris, "/event"), allOf(
                hasProperty("app", equalTo("ewm-main-service")),
                hasProperty("uri", equalTo("/event")),
                hasProperty("hits", equalTo(1L)
                )));
        assertThat(findByUri(hitsByUris, "/requests"), allOf(
                hasProperty("app", equalTo("ewm-main-service")),
                hasProperty("uri", equalTo("/requests")),
                hasProperty("hits", equalTo(1L)
                )));
    }


    @Test
    void shouldFindHitByUrisWhenStartTimeEqualEndTime() {
        List<Stats> result = statRepository.getHitsByUris(
                List.of(hit1.getUri()),
                start,
                start,
                JpaSort.unsafe(Sort.Direction.DESC, "count(h.uri)")
        );

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getUri(), equalTo(hit1.getUri()));
        assertThat(result.get(0).getHits(), equalTo(1L));
    }


    @Test
    void shouldFindUniqueHitByUrisWhenStartTimeEqualEndTime() {
        List<Stats> result = statRepository.getUniqueHitsByUris(
                List.of(hit1.getUri()),
                start,
                start,
                JpaSort.unsafe(Sort.Direction.DESC, "count(h.uri)")
        );

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getUri(), equalTo(hit1.getUri()));
        assertThat(result.get(0).getHits(), equalTo(1L));
    }

    @Test
    void shouldFindAllHitsWhenStartTimeEqualEndTime() {
        List<Stats> result = statRepository.getAllHits(
                start,
                start,
                JpaSort.unsafe(Sort.Direction.DESC, "count(h.uri)")
        );

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getUri(), equalTo(hit1.getUri()));
        assertThat(result.get(0).getHits(), equalTo(1L));
    }

    @Test
    void shouldFindAllUniqueHitsWhenStartTimeEqualEndTime() {
        List<Stats> result = statRepository.getAllUniqueHits(
                start,
                start,
                JpaSort.unsafe(Sort.Direction.DESC, "count(h.uri)")
        );

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getUri(), equalTo(hit1.getUri()));
        assertThat(result.get(0).getHits(), equalTo(1L));
    }


    @Test
    void shouldFindOnlyHitsWithinInclusiveTimeRange() {
        LocalDateTime end = start.plusSeconds(2);

        List<Stats> result = statRepository.getHitsByUris(
                List.of(hit1.getUri(), hit3.getUri(), hit4.getUri()),
                start,
                end,
                JpaSort.unsafe(Sort.Direction.DESC, "count(h.uri)")
        );

        assertThat(result, hasSize(2));
        assertThat(findByUri(result, "/event").getHits(), equalTo(2L));
        assertThat(findByUri(result, "/requests").getHits(), equalTo(1L));
    }

    private Stats findByUri(List<Stats> stats, String uri) {
        return stats.stream()
                .filter(item -> item.getUri().equals(uri))
                .findFirst()
                .orElseThrow();
    }
}
