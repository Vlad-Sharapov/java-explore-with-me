package ru.yandex.practicum.mainservice.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.yandex.practicum.mainservice.location.model.Location;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long>, QuerydslPredicateExecutor<Location> {

    @Query(value = "SELECT distance(?1,?2,?3,?4)", nativeQuery = true)
    Double coordinateLength(double lat1, double lon1, double lat2, double lon2);


    @Query(value = "SELECT * FROM location l " +
            "WHERE distance(?1,?2,l.lat,l.lon) < l.radius " +
            "ORDER BY distance(?1,?2,l.lat,l.lon) ASC LIMIT 1", nativeQuery = true)
    Optional<Location> findLocationByCoordinate(double lat1, double lon1);
}
