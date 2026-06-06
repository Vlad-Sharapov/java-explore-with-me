package ru.yandex.practicum.mainservice.event.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.yandex.practicum.mainservice.TestUtils;
import ru.yandex.practicum.mainservice.category.dto.CategoryMapper;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.event.dto.EventFullDto;
import ru.yandex.practicum.mainservice.event.dto.LocationMapper;
import ru.yandex.practicum.mainservice.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.enums.adminenum.AdmStateAction;
import ru.yandex.practicum.mainservice.event.model.GetEventsForAdminRequest;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.event.service.admin.EventAdminService;
import ru.yandex.practicum.mainservice.user.dto.UserMapper;
import ru.yandex.practicum.mainservice.user.model.User;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = EventAdminController.class)
class EventAdminControllerTest {

    @MockitoBean
    private EventAdminService eventService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldUpdateEvent() throws Exception {
        long eventId = 1L;
        User user = EventTestData.makeUser(1L);
        Category category = EventTestData.makeCategory(1L, "category");
        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);
        UpdateEventAdminRequest updateRequest = UpdateEventAdminRequest.builder()
                .title("Updated title")
                .annotation("Updated annotation annotation")
                .stateAction(AdmStateAction.PUBLISH_EVENT)
                .build();

        EventFullDto response = EventFullDto.builder()
                .id(eventId)
                .initiator(UserMapper.toUserDto(user))
                .category(CategoryMapper.toCategoryDto(category))
                .location(LocationMapper.toUserLocationDto(location))
                .title(updateRequest.getTitle())
                .annotation(updateRequest.getAnnotation())
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

        Mockito.when(eventService.update(Mockito.eq(eventId), Mockito.any(UpdateEventAdminRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .content(TestUtils.asJsonString(objectMapper, updateRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.state").value("PUBLISHED"));

        ArgumentCaptor<UpdateEventAdminRequest> requestCaptor =
                ArgumentCaptor.forClass(UpdateEventAdminRequest.class);
        Mockito.verify(eventService).update(Mockito.eq(eventId), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getTitle(), equalTo(updateRequest.getTitle()));
        assertThat(requestCaptor.getValue().getStateAction(), equalTo(AdmStateAction.PUBLISH_EVENT));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateEventInvalid() throws Exception {
        UpdateEventAdminRequest updateRequest = UpdateEventAdminRequest.builder()
                .title("up")
                .build();

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .content(TestUtils.asJsonString(objectMapper, updateRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verify(eventService, Mockito.never()).update(Mockito.anyLong(), Mockito.any());
    }

    @Test
    void shouldGetEvents() throws Exception {
        User user = EventTestData.makeUser(1L);
        Category category = EventTestData.makeCategory(1L, "category");
        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);

        EventFullDto response = EventFullDto.builder()
                .id(1L)
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

        Mockito.when(eventService.getEvents(Mockito.any(GetEventsForAdminRequest.class)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/admin/events")
                        .param("users", "1", "2")
                        .param("states", "PUBLISHED")
                        .param("categories", "1")
                        .param("rangeStart", "2026-01-10T12:00:00")
                        .param("rangeEnd", "2026-01-12T12:00:00")
                        .param("from", "20")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].state").value("PUBLISHED"));

        ArgumentCaptor<GetEventsForAdminRequest> requestCaptor =
                ArgumentCaptor.forClass(GetEventsForAdminRequest.class);
        Mockito.verify(eventService).getEvents(requestCaptor.capture());

        GetEventsForAdminRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getUsers(), equalTo(List.of(1L, 2L)));
        assertThat(capturedRequest.getStates(), equalTo(List.of(EventState.PUBLISHED)));
        assertThat(capturedRequest.getCategories(), equalTo(List.of(1L)));
        assertThat(capturedRequest.getFrom(), equalTo(20));
        assertThat(capturedRequest.getSize(), equalTo(10));
    }

    @Test
    void shouldGetEventsByLocation() throws Exception {
        long placeId = 1L;
        User user = EventTestData.makeUser(1L);
        Category category = EventTestData.makeCategory(1L, "category");
        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);
        EventFullDto response = EventFullDto.builder()
                .id(1L)
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
                .state(EventState.PENDING)
                .confirmedRequests(2L)
                .views(5L)
                .build();

        Mockito.when(eventService.getEventsByLocation(placeId, 20, 10))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/admin/events/{placeId}/locations", placeId)
                        .param("from", "20")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].location.lat").value(50.0));

        Mockito.verify(eventService).getEventsByLocation(placeId, 20, 10);
    }
}
