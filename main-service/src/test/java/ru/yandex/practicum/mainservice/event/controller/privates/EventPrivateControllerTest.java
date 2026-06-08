package ru.yandex.practicum.mainservice.event.controller.privates;

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
import ru.yandex.practicum.mainservice.event.dto.*;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.event.service.privates.EventPrivateService;
import ru.yandex.practicum.mainservice.exceptions.EntitiesConflictException;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.requests.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.mainservice.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.mainservice.requests.enums.RequestStatus;
import ru.yandex.practicum.mainservice.requests.model.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.mainservice.user.dto.UserMapper;
import ru.yandex.practicum.mainservice.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = EventPrivateController.class)
class EventPrivateControllerTest {

    private final long userId = 1L;
    private final long eventId = 2L;
    @MockitoBean
    private EventPrivateService eventService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateEvent() throws Exception {

        User user = EventTestData.makeUser(1L);

        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);

        Category category = EventTestData.makeCategory(1L, "category");

        NewEventDto newEventDto = EventTestData.makeEventDto(category, location)
                .eventDate(LocalDateTime.now().plusDays(3))
                .build();

        Mockito.when(eventService.add(Mockito.anyLong(), Mockito.any(NewEventDto.class)))
                .thenReturn(EventFullDto.builder()
                        .id(1L)
                        .initiator(UserMapper.toUserDto(user))
                        .title(newEventDto.getTitle())
                        .description(newEventDto.getDescription())
                        .annotation(newEventDto.getAnnotation())
                        .eventDate(newEventDto.getEventDate().format(EventTestData.FORMATTER))
                        .category(CategoryMapper.toCategoryDto(category))
                        .location(LocationMapper.toUserLocationDto(location))
                        .build());

        mockMvc.perform(post("/users/{userId}/events", userId, eventId)
                        .content(TestUtils.asJsonString(objectMapper, newEventDto))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.initiator.id").value(1L))
                .andExpect(jsonPath("$.title").value(newEventDto.getTitle()));

        ArgumentCaptor<NewEventDto> captor = ArgumentCaptor.forClass(NewEventDto.class);
        Mockito.verify(eventService).add(Mockito.anyLong(), captor.capture());
        NewEventDto eventDto = captor.getValue();
        assertThat(eventDto.getTitle(), equalTo(newEventDto.getTitle()));
        assertThat(eventDto.getDescription(), equalTo(newEventDto.getDescription()));


    }

    @Test
    void shouldReturnBadRequestWhenRequiredFieldIsMissing() throws Exception {

        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);

        Category category = EventTestData.makeCategory(1L, "category");

        NewEventDto newEventDto = EventTestData.makeEventDto(category, location)
                .title(null)
                .eventDate(LocalDateTime.now().plusDays(3))
                .build();

        Mockito.verify(eventService, Mockito.never())
                .add(Mockito.anyLong(), Mockito.any(NewEventDto.class));

        mockMvc.perform(post("/users/{userId}/events", userId, eventId)
                        .content(TestUtils.asJsonString(objectMapper, newEventDto))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        Mockito.verify(eventService, Mockito.never())
                .add(Mockito.anyLong(), Mockito.any(NewEventDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenStringSizeInvalid() throws Exception {

        User user = EventTestData.makeUser(1L);

        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);

        Category category = EventTestData.makeCategory(1L, "category");

        NewEventDto newEventDto = EventTestData.makeEventDto(category, location)
                .annotation("Annotation")
                .eventDate(LocalDateTime.now().plusDays(3))
                .build();

        Mockito.verify(eventService, Mockito.never())
                .add(Mockito.anyLong(), Mockito.any(NewEventDto.class));

        mockMvc.perform(post("/users/{userId}/events", userId, eventId)
                        .content(TestUtils.asJsonString(objectMapper, newEventDto))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        Mockito.verify(eventService, Mockito.never())
                .add(Mockito.anyLong(), Mockito.any(NewEventDto.class));
    }


    @Test
    void shouldReturnBadRequestWhenEventDateTooSoon() throws Exception {
        User user = EventTestData.makeUser(1L);

        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);

        Category category = EventTestData.makeCategory(1L, "category");

        NewEventDto newEventDto = EventTestData.makeEventDto(category, location)
                .eventDate(LocalDateTime.now().plusHours(1))
                .build();

        Mockito.verify(eventService, Mockito.never())
                .add(Mockito.anyLong(), Mockito.any(NewEventDto.class));

        mockMvc.perform(post("/users/{userId}/events", userId, eventId)
                        .content(TestUtils.asJsonString(objectMapper, newEventDto))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        Mockito.verify(eventService, Mockito.never())
                .add(Mockito.anyLong(), Mockito.any(NewEventDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenParticipantLimitNegative() throws Exception {

        User user = EventTestData.makeUser(1L);

        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);

        Category category = EventTestData.makeCategory(1L, "category");

        NewEventDto newEventDto = EventTestData.makeEventDto(category, location)
                .participantLimit(-1)
                .eventDate(LocalDateTime.now().plusDays(3))
                .build();

        Mockito.verify(eventService, Mockito.never())
                .add(Mockito.anyLong(), Mockito.any(NewEventDto.class));

        mockMvc.perform(post("/users/{userId}/events", userId, eventId)
                        .content(TestUtils.asJsonString(objectMapper, newEventDto))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        Mockito.verify(eventService, Mockito.never())
                .add(Mockito.anyLong(), Mockito.any(NewEventDto.class));
    }


    @Test
    void shouldUpdateEvent() throws Exception {

        User user = EventTestData.makeUser(1L);

        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);

        Category category = EventTestData.makeCategory(1L, "category");

        UpdateEventUserRequest updateEventDto = UpdateEventUserRequest
                .builder()
                .annotation("annotation annotation annotation")
                .title("updated title")
                .category(1L)
                .location(location)
                .description("qwer tyui op[a sdfg hjkl;x")
                .eventDate(LocalDateTime.now().plusDays(3))
                .paid(false)
                .requestModeration(true)
                .participantLimit(5)
                .build();

        Mockito.when(eventService
                        .update(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(UpdateEventUserRequest.class)))
                .thenReturn(EventFullDto.builder()
                        .id(1L)
                        .initiator(UserMapper.toUserDto(user))
                        .title(updateEventDto.getTitle())
                        .description(updateEventDto.getDescription())
                        .annotation(updateEventDto.getAnnotation())
                        .eventDate(updateEventDto.getEventDate().format(EventTestData.FORMATTER))
                        .category(CategoryMapper.toCategoryDto(category))
                        .location(LocationMapper.toUserLocationDto(location))
                        .build());

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .content(TestUtils.asJsonString(objectMapper, updateEventDto))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.initiator.id").value(1L))
                .andExpect(jsonPath("$.title").value(updateEventDto.getTitle()));

    }

    @Test
    void shouldReturnBadRequestWhenUpdateFieldInvalid() throws Exception {

        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);

        UpdateEventUserRequest updateEventDto = UpdateEventUserRequest
                .builder()
                .annotation("annotation annotation annotation")
                .title("up")
                .category(1L)
                .location(location)
                .description("qwer tyui op[a sdfg hjkl;x")
                .eventDate(LocalDateTime.now().plusDays(3))
                .paid(false)
                .requestModeration(true)
                .participantLimit(5)
                .build();

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .content(TestUtils.asJsonString(objectMapper, updateEventDto))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        Mockito.verify(eventService, Mockito.never())
                .update(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(UpdateEventUserRequest.class));

    }

    @Test
    void shouldReturnBadRequestWhenUpdateEventDateTooSoon() throws Exception {

        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);

        UpdateEventUserRequest updateEventDto = UpdateEventUserRequest
                .builder()
                .annotation("annotation annotation annotation")
                .title("updated title")
                .category(1L)
                .location(location)
                .description("qwer tyui op[a sdfg hjkl;x")
                .eventDate(LocalDateTime.now().plusHours(1))
                .paid(false)
                .requestModeration(true)
                .participantLimit(5)
                .build();

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .content(TestUtils.asJsonString(objectMapper, updateEventDto))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        Mockito.verify(eventService, Mockito.never())
                .update(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(UpdateEventUserRequest.class));

    }


    @Test
    void shouldChangeStatus() throws Exception {

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(10L, 11L));
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        EventRequestStatusUpdateResult updateResult = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of(
                        ParticipationRequestDto.builder()
                                .id(10L)
                                .event(eventId)
                                .requester(3L)
                                .status(RequestStatus.CONFIRMED)
                                .created(EventTestData.FIXED_TIME.format(EventTestData.FORMATTER))
                                .build(),
                        ParticipationRequestDto.builder()
                                .id(11L)
                                .event(eventId)
                                .requester(4L)
                                .status(RequestStatus.CONFIRMED)
                                .created(EventTestData.FIXED_TIME.format(EventTestData.FORMATTER))
                                .build()
                ))
                .rejectedRequests(List.of())
                .build();

        Mockito.when(eventService.confirmRequests(
                        Mockito.eq(userId),
                        Mockito.eq(eventId),
                        Mockito.any(EventRequestStatusUpdateRequest.class)))
                .thenReturn(updateResult);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .content(TestUtils.asJsonString(objectMapper, updateRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests.length()").value(2))
                .andExpect(jsonPath("$.confirmedRequests[0].id").value(10L))
                .andExpect(jsonPath("$.confirmedRequests[0].event").value(eventId))
                .andExpect(jsonPath("$.confirmedRequests[0].requester").value(3L))
                .andExpect(jsonPath("$.confirmedRequests[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$.confirmedRequests[1].id").value(11L))
                .andExpect(jsonPath("$.rejectedRequests.length()").value(0));

        ArgumentCaptor<EventRequestStatusUpdateRequest> requestCaptor =
                ArgumentCaptor.forClass(EventRequestStatusUpdateRequest.class);

        Mockito.verify(eventService).confirmRequests(Mockito.eq(userId), Mockito.eq(eventId), requestCaptor.capture());

        EventRequestStatusUpdateRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getRequestIds(), equalTo(updateRequest.getRequestIds()));
        assertThat(capturedRequest.getStatus(), equalTo(RequestStatus.CONFIRMED));

    }

    @Test
    void shouldReturnConflictRequestWhenConfirmRequestsThrowEntitiesConflictException() throws Exception {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(10L, 11L));
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        Mockito.when(eventService.confirmRequests(
                        Mockito.eq(userId),
                        Mockito.eq(eventId),
                        Mockito.any(EventRequestStatusUpdateRequest.class)))
                .thenThrow(new EntitiesConflictException("The user can only change his own events"));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .content(TestUtils.asJsonString(objectMapper, updateRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        Mockito.verify(eventService).confirmRequests(Mockito.anyLong(),
                Mockito.anyLong(),
                Mockito.any(EventRequestStatusUpdateRequest.class));
    }


    @Test
    void shouldReturnBadRequestWhenServiceThrowEntityNotFoundException() throws Exception {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(10L, 11L));
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        Mockito.when(eventService.confirmRequests(
                        Mockito.eq(userId),
                        Mockito.eq(eventId),
                        Mockito.any(EventRequestStatusUpdateRequest.class)))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .content(TestUtils.asJsonString(objectMapper, updateRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(eventService).confirmRequests(Mockito.anyLong(),
                Mockito.anyLong(),
                Mockito.any(EventRequestStatusUpdateRequest.class));
    }


    @Test
    void events() throws Exception {
        User user = EventTestData.makeUser(userId);
        Category category = EventTestData.makeCategory(1L, "category");

        EventShortDto eventShortDto = EventShortDto.builder()
                .id(eventId)
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

        Mockito.when(eventService.getAllUserEvents(userId, 20, 10))
                .thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/users/{userId}/events", userId)
                        .param("from", "20")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(eventId))
                .andExpect(jsonPath("$[0].initiator.id").value(userId))
                .andExpect(jsonPath("$[0].title").value("Concert"))
                .andExpect(jsonPath("$[0].confirmedRequests").value(2L))
                .andExpect(jsonPath("$[0].views").value(5L));

        Mockito.verify(eventService).getAllUserEvents(userId, 20, 10);
    }

    @Test
    void event() throws Exception {
        User user = EventTestData.makeUser(userId);
        Category category = EventTestData.makeCategory(1L, "category");
        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);

        EventFullDto eventFullDto = EventFullDto.builder()
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
                .state(EventState.PENDING)
                .confirmedRequests(2L)
                .views(5L)
                .build();

        Mockito.when(eventService.getUserEvent(userId, eventId))
                .thenReturn(eventFullDto);

        mockMvc.perform(get("/users/{userId}/events/{eventId}", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.initiator.id").value(userId))
                .andExpect(jsonPath("$.title").value("Concert"))
                .andExpect(jsonPath("$.location.lat").value(50.0))
                .andExpect(jsonPath("$.participantLimit").value(10))
                .andExpect(jsonPath("$.requestModeration").value(true))
                .andExpect(jsonPath("$.state").value("PENDING"));

        Mockito.verify(eventService).getUserEvent(userId, eventId);
    }

    @Test
    void eventRequests() throws Exception {
        ParticipationRequestDto requestDto = ParticipationRequestDto.builder()
                .id(10L)
                .event(eventId)
                .requester(3L)
                .status(RequestStatus.PENDING)
                .created(EventTestData.FIXED_TIME.format(EventTestData.FORMATTER))
                .build();

        Mockito.when(eventService.getUserEventRequests(userId, eventId))
                .thenReturn(List.of(requestDto));

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].event").value(eventId))
                .andExpect(jsonPath("$[0].requester").value(3L))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        Mockito.verify(eventService).getUserEventRequests(userId, eventId);
    }
}
