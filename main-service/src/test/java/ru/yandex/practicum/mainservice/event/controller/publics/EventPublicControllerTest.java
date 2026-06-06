package ru.yandex.practicum.mainservice.event.controller.publics;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mainservice.EventTestData;
import ru.yandex.practicum.mainservice.category.dto.CategoryMapper;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.event.dto.EventFullDto;
import ru.yandex.practicum.mainservice.event.dto.EventShortDto;
import ru.yandex.practicum.mainservice.event.dto.LocationMapper;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.model.GetEventsForPublicRequest;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.event.service.publics.EventPublicService;
import ru.yandex.practicum.mainservice.event.stateclient.ClientHandler;
import ru.yandex.practicum.mainservice.user.dto.UserMapper;
import ru.yandex.practicum.mainservice.user.model.User;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = EventPublicController.class)
class EventPublicControllerTest {

    @MockitoBean
    private EventPublicService eventService;

    @MockitoBean
    private ClientHandler clientHandler;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetEvents() throws Exception {
        User user = EventTestData.makeUser(1L);
        Category category = EventTestData.makeCategory(1L, "category");
        EventShortDto response = EventShortDto.builder()
                .id(1L)
                .initiator(UserMapper.toUserDto(user))
                .category(CategoryMapper.toCategoryDto(category))
                .title("Concert")
                .annotation("annotation annotation annotation")
                .description("description description description")
                .eventDate(EventTestData.FIXED_TIME.plusDays(1).format(EventTestData.FORMATTER))
                .paid(false)
                .confirmedRequests(2L)
                .views(5L)
                .build();

        Mockito.when(eventService.getEvents(Mockito.any(GetEventsForPublicRequest.class)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/events")
                        .param("text", "concert")
                        .param("categories", "1", "2")
                        .param("paid", "false")
                        .param("rangeStart", "2026-01-10T12:00:00")
                        .param("rangeEnd", "2026-01-12T12:00:00")
                        .param("onlyAvailable", "true")
                        .param("sort", "views")
                        .param("from", "20")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Concert"))
                .andExpect(jsonPath("$[0].views").value(5L));

        ArgumentCaptor<GetEventsForPublicRequest> requestCaptor =
                ArgumentCaptor.forClass(GetEventsForPublicRequest.class);
        Mockito.verify(eventService).getEvents(requestCaptor.capture());
        Mockito.verify(clientHandler).addHit(Mockito.any(HttpServletRequest.class));

        GetEventsForPublicRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getText(), equalTo("concert"));
        assertThat(capturedRequest.getCategories(), equalTo(List.of(1L, 2L)));
        assertThat(capturedRequest.getPaid(), equalTo(false));
        assertThat(capturedRequest.getOnlyAvailable(), equalTo(true));
        assertThat(capturedRequest.getSort(), equalTo(GetEventsForPublicRequest.Sort.VIEWS));
        assertThat(capturedRequest.getFrom(), equalTo(20));
        assertThat(capturedRequest.getSize(), equalTo(10));
    }

    @Test
    void shouldGetEventsByLocation() throws Exception {
        long placeId = 1L;
        User user = EventTestData.makeUser(1L);
        Category category = EventTestData.makeCategory(1L, "category");
        EventShortDto response = EventShortDto.builder()
                .id(1L)
                .initiator(UserMapper.toUserDto(user))
                .category(CategoryMapper.toCategoryDto(category))
                .title("Concert")
                .annotation("annotation annotation annotation")
                .description("description description description")
                .eventDate(EventTestData.FIXED_TIME.plusDays(1).format(EventTestData.FORMATTER))
                .paid(false)
                .confirmedRequests(2L)
                .views(5L)
                .build();

        Mockito.when(eventService.getEventsByLocation(placeId, 20, 10))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/events/{placeId}/locations", placeId)
                        .param("from", "20")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Concert"));

        Mockito.verify(eventService).getEventsByLocation(placeId, 20, 10);
        Mockito.verify(clientHandler, Mockito.never()).addHit(Mockito.any());
    }

    @Test
    void shouldGetEvent() throws Exception {
        long eventId = 1L;
        User user = EventTestData.makeUser(1L);
        Category category = EventTestData.makeCategory(1L, "category");
        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);
        EventFullDto response = EventFullDto.builder()
                .id(eventId)
                .initiator(UserMapper.toUserDto(user))
                .category(CategoryMapper.toCategoryDto(category))
                .location(LocationMapper.toUserLocationDto(location))
                .title("Concert")
                .annotation("annotation annotation annotation")
                .description("description description description")
                .eventDate(EventTestData.FIXED_TIME.plusDays(1).format(EventTestData.FORMATTER))
                .createdOn(EventTestData.FIXED_TIME.format(EventTestData.FORMATTER))
                .publishedOn(EventTestData.FIXED_TIME.plusHours(1).format(EventTestData.FORMATTER))
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .confirmedRequests(2L)
                .views(5L)
                .build();

        Mockito.when(eventService.getEvent(eventId))
                .thenReturn(response);

        mockMvc.perform(get("/events/{eventId}", eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.title").value("Concert"))
                .andExpect(jsonPath("$.location.lat").value(50.0))
                .andExpect(jsonPath("$.state").value("PUBLISHED"));

        Mockito.verify(eventService).getEvent(eventId);
        Mockito.verify(clientHandler).addHit(Mockito.any(HttpServletRequest.class));
    }
}
