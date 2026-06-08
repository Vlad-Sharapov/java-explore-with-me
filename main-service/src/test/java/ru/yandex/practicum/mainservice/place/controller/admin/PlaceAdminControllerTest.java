package ru.yandex.practicum.mainservice.place.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mainservice.TestUtils;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.place.dto.PlaceDto;
import ru.yandex.practicum.mainservice.place.service.admin.LocationAdminService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(PlaceAdminController.class)
class PlaceAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LocationAdminService locationAdminService;

    @Test
    void shouldAddPlace() throws Exception {
        PlaceDto newPlaceDto = PlaceDto.builder()
                .lat(45.0)
                .lon(50.0)
                .radius(9.0)
                .name("Test place")
                .build();

        PlaceDto placeDto = newPlaceDto.toBuilder()
                .id(1L)
                .build();

        Mockito.when(locationAdminService.add(Mockito.any(PlaceDto.class)))
                .thenReturn(placeDto);

        mockMvc.perform(post("/admin/locations")
                        .content(TestUtils.asJsonString(objectMapper, newPlaceDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(placeDto.getId()))
                .andExpect(jsonPath("$.lat").value(placeDto.getLat()))
                .andExpect(jsonPath("$.lon").value(placeDto.getLon()))
                .andExpect(jsonPath("$.radius").value(placeDto.getRadius()))
                .andExpect(jsonPath("$.name").value(placeDto.getName()));

        Mockito.verify(locationAdminService).add(Mockito.any(PlaceDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenRadiusIsNegative() throws Exception {
        PlaceDto placeDto = PlaceDto.builder()
                .lat(45.0)
                .lon(50.0)
                .radius(-1.0)
                .name("Test place")
                .build();

        mockMvc.perform(post("/admin/locations")
                        .content(TestUtils.asJsonString(objectMapper, placeDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.message").value("Field: radius. Error: The field cannot be negative"))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(locationAdminService, Mockito.never()).add(Mockito.any(PlaceDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenNameIsTooShort() throws Exception {
        PlaceDto placeDto = PlaceDto.builder()
                .lat(45.0)
                .lon(50.0)
                .radius(9.0)
                .name("A")
                .build();

        mockMvc.perform(post("/admin/locations")
                        .content(TestUtils.asJsonString(objectMapper, placeDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.message").value("Field: name. Error: Must not be less than 2 characters and more than 250 characters."))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(locationAdminService, Mockito.never()).add(Mockito.any(PlaceDto.class));
    }

    @Test
    void shouldUpdatePlace() throws Exception {
        PlaceDto updatePlaceDto = PlaceDto.builder()
                .id(1L)
                .name("Updated place")
                .radius(11.0)
                .build();

        PlaceDto placeDto = PlaceDto.builder()
                .id(1L)
                .lat(45.0)
                .lon(50.0)
                .radius(11.0)
                .name("Updated place")
                .build();

        Mockito.when(locationAdminService.update(Mockito.any(PlaceDto.class)))
                .thenReturn(placeDto);

        mockMvc.perform(patch("/admin/locations")
                        .content(TestUtils.asJsonString(objectMapper, updatePlaceDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(placeDto.getId()))
                .andExpect(jsonPath("$.lat").value(placeDto.getLat()))
                .andExpect(jsonPath("$.lon").value(placeDto.getLon()))
                .andExpect(jsonPath("$.radius").value(placeDto.getRadius()))
                .andExpect(jsonPath("$.name").value(placeDto.getName()));

        Mockito.verify(locationAdminService).update(Mockito.any(PlaceDto.class));
    }

    @Test
    void shouldThrowNotFoundWhenUpdatePlaceDoesNotExist() throws Exception {
        PlaceDto updatePlaceDto = PlaceDto.builder()
                .id(1L)
                .name("Updated place")
                .build();

        Mockito.when(locationAdminService.update(Mockito.any(PlaceDto.class)))
                .thenThrow(new EntityNotFoundException("location with id=1 was not found"));

        mockMvc.perform(patch("/admin/locations")
                        .content(TestUtils.asJsonString(objectMapper, updatePlaceDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404 NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("The required object was not found."))
                .andExpect(jsonPath("$.message").value("location with id=1 was not found"))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(locationAdminService).update(Mockito.any(PlaceDto.class));
    }

    @Test
    void shouldDeletePlace() throws Exception {
        mockMvc.perform(delete("/admin/locations/{locationId}", 1L))
                .andExpect(status().isNoContent());

        Mockito.verify(locationAdminService).delete(1L);
    }

    @Test
    void shouldThrowNotFoundWhenDeletePlaceDoesNotExist() throws Exception {
        Mockito.doThrow(new EntityNotFoundException("location with id=1 was not found"))
                .when(locationAdminService)
                .delete(Mockito.anyLong());

        mockMvc.perform(delete("/admin/locations/{locationId}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404 NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("The required object was not found."))
                .andExpect(jsonPath("$.message").value(containsString("location with id=1")))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(locationAdminService).delete(1L);
    }

    @Test
    void shouldGetPlaces() throws Exception {
        PlaceDto place1 = PlaceDto.builder()
                .id(1L)
                .lat(45.0)
                .lon(50.0)
                .radius(9.0)
                .name("First place")
                .build();
        PlaceDto place2 = PlaceDto.builder()
                .id(2L)
                .lat(46.0)
                .lon(51.0)
                .radius(11.0)
                .name("Second place")
                .build();

        Mockito.when(locationAdminService.getPlaces(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(place1, place2));

        mockMvc.perform(get("/admin/locations")
                        .param("text", "place")
                        .param("from", "20")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(place1.getId()))
                .andExpect(jsonPath("$[0].name").value(place1.getName()))
                .andExpect(jsonPath("$[1].id").value(place2.getId()))
                .andExpect(jsonPath("$[1].name").value(place2.getName()));

        Mockito.verify(locationAdminService).getPlaces("place", 20, 10);
    }

    @Test
    void shouldGetPlacesWithDefaultParams() throws Exception {
        Mockito.when(locationAdminService.getPlaces(Mockito.isNull(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(locationAdminService).getPlaces(null, 0, 10);
    }
}
