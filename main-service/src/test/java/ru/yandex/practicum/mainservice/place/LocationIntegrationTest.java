package ru.yandex.practicum.mainservice.place;


import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.mainservice.TestUtils;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.place.dto.PlaceDto;
import ru.yandex.practicum.mainservice.support.IntegrationTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.yandex.practicum.mainservice.TestUtils.extractId;

public class LocationIntegrationTest extends IntegrationTestSupport {

    @Test
    void shouldFindEventByLocation() throws Exception {
        Location location1 = Location.builder()
                .lat(55.78)
                .lon(37.59)
                .build();

        Location location2 = Location.builder()
                .lat(75.78)
                .lon(37.59)
                .build();
        Long initiator = createUser("initiator", "initiator@mail.ru");
        Long category = createCategory("category");
        Long eventInPlace = createEvent(initiator, category, false, 10, location1);
        createEvent(initiator, category, false, 10, location2);
        Long moscowPlaceId = createPlace(55.75, 37.61, 10.0, "Moscow center");

        mockMvc.perform(get("/admin/events/{placeId}/locations", moscowPlaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(eventInPlace));

        publishEvent(eventInPlace);

        mockMvc.perform(get("/events/{placeId}/locations", moscowPlaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(eventInPlace));

    }

    private Long createPlace(Double lat, Double lon, Double radius, String name) throws Exception {

        PlaceDto newPlaceDto = PlaceDto.builder()
                .lat(lat)
                .lon(lon)
                .radius(radius)
                .name(name)
                .build();

        MvcResult placeResult = mockMvc.perform(post("/admin/locations")
                        .content(TestUtils.asJsonString(objectMapper, newPlaceDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lat").value(newPlaceDto.getLat()))
                .andExpect(jsonPath("$.lon").value(newPlaceDto.getLon()))
                .andExpect(jsonPath("$.radius").value(newPlaceDto.getRadius()))
                .andExpect(jsonPath("$.name").value(newPlaceDto.getName()))
                .andReturn();

        return extractId(objectMapper, placeResult);
    }

}
