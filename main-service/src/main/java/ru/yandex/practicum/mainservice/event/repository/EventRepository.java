package ru.yandex.practicum.mainservice.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.model.Event;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    Optional<Event> findByIdAndState(long id, EventState state);

    @Query("select e " +
            "from Event e " +
            "JOIN e.category " +
            "JOIN e.initiator " +
            "JOIN e.location " +
            "WHERE e.id IN ?1")
    Set<Event> findAllByIdAsSet(Collection<Long> id);


    @Query(value = "SELECT * FROM events e " +
            "JOIN location l ON e.location_id = l.id " +
            "WHERE distance(?1,?2,l.lat,l.lon) < ?3 " +
            "ORDER BY distance(?1,?2,l.lat,l.lon)", nativeQuery = true)
    List<Event> findEventsByLocation(double lat1, double lon1, double radius);

    @Query(value = "SELECT * FROM events e " +
            "JOIN location l ON e.location_id = l.id " +
            "WHERE distance(?1,?2,l.lat,l.lon) < ?3 " +
            "AND e.state = 'PUBLISHED' " +
            "ORDER BY distance(?1,?2,l.lat,l.lon)", nativeQuery = true)
    List<Event> findPublishedEventsByLocation(double lat1, double lon1, double radius);
}
