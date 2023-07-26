package ru.yandex.practicum.mainservice.event.repository;

import com.querydsl.core.types.Predicate;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.model.Event;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    @Query(" select e " +
            "from Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "JOIN FETCH e.location " +
            "WHERE e.id = ?1")
    Optional<Event> findByIdEager(long eventId);

    @EntityGraph(attributePaths = {"category", "initiator", "location"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<Event> findByIdAndState(long id, EventState state);

    @EntityGraph(attributePaths = {"category", "initiator", "location"}, type = EntityGraph.EntityGraphType.LOAD)
    @NonNull
    Page<Event> findAll(@NonNull Predicate predicate, @NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"category", "initiator", "location"}, type = EntityGraph.EntityGraphType.LOAD)
    @NonNull
    Page<Event> findAll(@NonNull Pageable pageable);

    @Query("select e " +
            "from Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "JOIN FETCH e.location " +
            "WHERE e.id IN ?1")
    Set<Event> findAllByIdEager(Collection<Long> id);
}
