package ru.yandex.practicum.mainservice.place.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.mainservice.place.model.Place;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query(value = "SELECT * FROM place l " +
            "WHERE distance(?1,?2,l.lat,l.lon) < l.radius " +
            "ORDER BY distance(?1,?2,l.lat,l.lon) ASC LIMIT 1", nativeQuery = true)
    Optional<Place> findPlaceByLocation(double lat1, double lon1);


    List<Place> findAllByNameLikeIgnoreCase(String name, Pageable pageable);

}
