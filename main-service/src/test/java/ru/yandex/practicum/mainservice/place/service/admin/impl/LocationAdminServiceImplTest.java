package ru.yandex.practicum.mainservice.place.service.admin.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.place.dto.PlaceDto;
import ru.yandex.practicum.mainservice.place.model.Place;
import ru.yandex.practicum.mainservice.place.repository.PlaceRepository;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LocationAdminServiceImplTest {

    @InjectMocks
    private LocationAdminServiceImpl locationAdminService;

    @Mock
    private PlaceRepository placeRepository;

    @Test
    void shouldAddNewPlace() {
        PlaceDto placeDto = PlaceDto.builder()
                .lon(50.0)
                .lat(45.0)
                .radius(9.0)
                .name("Test name")
                .build();

        Mockito.when(placeRepository.save(Mockito.any()))
                .thenAnswer(
                        invocation -> {
                            Place argument = invocation.getArgument(0, Place.class);
                            argument.setId(1L);
                            return argument;
                        }
                );

        PlaceDto result = locationAdminService.add(placeDto);

        ArgumentCaptor<Place> argumentCaptor = ArgumentCaptor.forClass(Place.class);

        Mockito.verify(placeRepository, Mockito.times(1))
                .save(argumentCaptor.capture());

        Place capturedPlace = argumentCaptor.getValue();

        assertThat(capturedPlace.getId(), equalTo(1L));
        assertThat(capturedPlace.getLat(), equalTo(45.0));
        assertThat(capturedPlace.getLon(), equalTo(50.0));
        assertThat(capturedPlace.getName(), equalTo("Test name"));
        assertThat(capturedPlace.getRadius(), equalTo(9.0));

        assertThat(result.getId(), equalTo(1L));
        assertThat(result.getRadius(), equalTo(9.0));
        assertThat(result.getLat(), equalTo(45.0));
        assertThat(result.getName(), equalTo("Test name"));
        assertThat(result.getLon(), equalTo(50.0));

    }

    @Test
    void shouldUpdatePlace() {
        Place oldPlace = Place.builder()
                .id(1L)
                .lon(50.0)
                .lat(45.0)
                .radius(9.0)
                .name("Test name")
                .build();

        Place upPlace = Place.builder()
                .id(1L)
                .lon(50.0)
                .lat(45.0)
                .radius(11.0)
                .name("Up name")
                .build();

        PlaceDto updatedPlaceDto = PlaceDto.builder()
                .id(1L)
                .name("Up name")
                .radius(11.0)
                .build();

        Mockito.when(placeRepository.save(Mockito.any()))
                .thenReturn(upPlace);

        Mockito.when(placeRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(oldPlace));

        PlaceDto result = locationAdminService.update(updatedPlaceDto);

        ArgumentCaptor<Place> argumentCaptor = ArgumentCaptor.forClass(Place.class);

        Mockito.verify(placeRepository, Mockito.times(1))
                .save(argumentCaptor.capture());

        Place capturedPlace = argumentCaptor.getValue();

        assertThat(capturedPlace.getId(), equalTo(1L));
        assertThat(capturedPlace.getLat(), equalTo(45.0));
        assertThat(capturedPlace.getLon(), equalTo(50.0));
        assertThat(capturedPlace.getName(), equalTo("Up name"));
        assertThat(capturedPlace.getRadius(), equalTo(11.0));

        assertThat(result.getId(), equalTo(1L));
        assertThat(result.getRadius(), equalTo(11.0));
        assertThat(result.getLat(), equalTo(45.0));
        assertThat(result.getName(), equalTo("Up name"));
        assertThat(result.getLon(), equalTo(50.0));

    }

    @Test
    void shouldUpdatePlaceWithoutChangingFields() {
        Place place = Place.builder()
                .id(1L)
                .lon(50.0)
                .lat(45.0)
                .radius(9.0)
                .name("Test name")
                .build();

        PlaceDto updatedPlaceDto = PlaceDto.builder()
                .id(1L)
                .build();

        Mockito.when(placeRepository.save(Mockito.any()))
                .thenReturn(place);

        Mockito.when(placeRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(place));

        PlaceDto result = locationAdminService.update(updatedPlaceDto);

        ArgumentCaptor<Place> argumentCaptor = ArgumentCaptor.forClass(Place.class);

        Mockito.verify(placeRepository, Mockito.times(1))
                .save(argumentCaptor.capture());

        Place capturedPlace = argumentCaptor.getValue();

        assertThat(capturedPlace.getId(), equalTo(1L));
        assertThat(capturedPlace.getLat(), equalTo(45.0));
        assertThat(capturedPlace.getLon(), equalTo(50.0));
        assertThat(capturedPlace.getName(), equalTo("Test name"));
        assertThat(capturedPlace.getRadius(), equalTo(9.0));

        assertThat(result.getId(), equalTo(1L));
        assertThat(result.getRadius(), equalTo(9.0));
        assertThat(result.getLat(), equalTo(45.0));
        assertThat(result.getName(), equalTo("Test name"));
        assertThat(result.getLon(), equalTo(50.0));
    }

    @Test
    void shouldThrowExceptionWhenPlaceDoesNotExist() {
        Mockito.when(placeRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> locationAdminService.update(PlaceDto.builder().id(1L).build()));
    }

    @Test
    void shouldGetPlacesWithoutTextFilter() {
        Place place1 = Place.builder()
                .id(1L)
                .lon(50.0)
                .lat(45.0)
                .radius(9.0)
                .name("First place")
                .build();

        Place place2 = Place.builder()
                .id(2L)
                .lon(51.0)
                .lat(46.0)
                .radius(11.0)
                .name("Second place")
                .build();

        Mockito.when(placeRepository.findAll(Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(place1, place2)));

        List<PlaceDto> result = locationAdminService.getPlaces(null, 20, 10);

        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        Mockito.verify(placeRepository).findAll(captor.capture());
        Mockito.verify(placeRepository, Mockito.never())
                .findAllByNameLikeIgnoreCase(Mockito.anyString(), Mockito.any(PageRequest.class));

        PageRequest capturedPageRequest = captor.getValue();
        assertThat(capturedPageRequest.getPageNumber(), equalTo(2));
        assertThat(capturedPageRequest.getPageSize(), equalTo(10));

        assertThat(result, hasSize(2));
        assertThat(result.stream()
                .map(PlaceDto::getId)
                .toList(), contains(1L, 2L));
        assertThat(result.stream()
                .map(PlaceDto::getName)
                .toList(), contains("First place", "Second place"));
    }

    @Test
    void shouldGetPlacesByText() {
        Place place = Place.builder()
                .id(1L)
                .lon(50.0)
                .lat(45.0)
                .radius(9.0)
                .name("Central park")
                .build();

        Mockito.when(placeRepository.findAllByNameLikeIgnoreCase(Mockito.anyString(), Mockito.any(PageRequest.class)))
                .thenReturn(List.of(place));

        List<PlaceDto> result = locationAdminService.getPlaces("par", 0, 10);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        Mockito.verify(placeRepository)
                .findAllByNameLikeIgnoreCase(textCaptor.capture(), pageRequestCaptor.capture());
        Mockito.verify(placeRepository, Mockito.never()).findAll(Mockito.any(PageRequest.class));

        assertThat(textCaptor.getValue(), equalTo("%par%"));
        assertThat(pageRequestCaptor.getValue().getPageNumber(), equalTo(0));
        assertThat(pageRequestCaptor.getValue().getPageSize(), equalTo(10));

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(1L));
        assertThat(result.get(0).getName(), equalTo("Central park"));
    }

    @Test
    void shouldDeletePlace() {
        Place place = Place.builder()
                .id(1L)
                .lon(50.0)
                .lat(45.0)
                .radius(9.0)
                .name("Test name")
                .build();

        Mockito.when(placeRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(place));

        locationAdminService.delete(place.getId());

        Mockito.verify(placeRepository).findById(place.getId());
        Mockito.verify(placeRepository).delete(place);
    }

    @Test
    void shouldThrowExceptionWhenDeletePlaceDoesNotExist() {
        Mockito.when(placeRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> locationAdminService.delete(1L));

        Mockito.verify(placeRepository, Mockito.never()).delete(Mockito.any(Place.class));
    }
}
