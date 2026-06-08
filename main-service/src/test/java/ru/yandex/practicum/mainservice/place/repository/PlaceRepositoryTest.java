package ru.yandex.practicum.mainservice.place.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mainservice.place.model.Place;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@DataJpaTest
class PlaceRepositoryTest {

    @Autowired
    EntityManager entityManager;
    @Autowired
    private PlaceRepository placeRepository;

    @Test
    void findAllByNameLikeIgnoreCase() {
        presistPlace("Central park", 50.0, 45.0);
        Place place1 = presistPlace("First place", 50.0, 45.0);
        Place place2 = presistPlace("Second place", 50.0, 45.0);
        flushAndClear();

        List<Place> result = placeRepository
                .findAllByNameLikeIgnoreCase("%pl%", PageRequest.of(0, 10));

        assertThat(result, hasSize(2));
        List<String> namesList = result.stream()
                .map(Place::getName)
                .toList();

        assertThat(namesList, containsInAnyOrder(place1.getName(), place2.getName()));
    }

    @Test
    void shouldFindPlacesByNameIgnoreCase() {
        Place place = presistPlace("Central Park", 50.0, 45.0);
        presistPlace("Museum", 50.0, 45.0);
        flushAndClear();

        List<Place> result = placeRepository
                .findAllByNameLikeIgnoreCase("%park%", PageRequest.of(0, 10));

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getName(), equalTo(place.getName()));
    }


    @Test
    void shouldFindPlacesWithPagination() {
        presistPlace("First place", 50.0, 45.0);
        presistPlace("Second place", 50.0, 45.0);
        presistPlace("Third place", 50.0, 45.0);
        flushAndClear();

        List<Place> result = placeRepository
                .findAllByNameLikeIgnoreCase("%place%", PageRequest.of(0, 2));

        assertThat(result, hasSize(2));
    }

    @Test
    void shouldReturnEmptyResultWhenNameDoesNotMatch() {
        presistPlace("Central park", 50.0, 45.0);
        presistPlace("Museum", 50.0, 45.0);
        flushAndClear();

        List<Place> result = placeRepository
                .findAllByNameLikeIgnoreCase("%unknown%", PageRequest.of(0, 10));

        assertThat(result, hasSize(0));
    }


    private Place presistPlace(String name, Double lat, Double lon) {
        Place place = Place.builder()
                .name(name)
                .lat(lat)
                .lon(lon)
                .build();
        entityManager.persist(place);
        return place;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}