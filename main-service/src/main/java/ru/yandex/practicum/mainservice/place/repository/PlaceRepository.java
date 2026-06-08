package ru.yandex.practicum.mainservice.place.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.mainservice.place.model.Place;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    List<Place> findAllByNameLikeIgnoreCase(String name, Pageable pageable);

}
