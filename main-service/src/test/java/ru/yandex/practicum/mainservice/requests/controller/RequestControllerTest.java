package ru.yandex.practicum.mainservice.requests.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mainservice.EventTestData;
import ru.yandex.practicum.mainservice.exceptions.EntitiesConflictException;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.mainservice.requests.enums.RequestStatus;
import ru.yandex.practicum.mainservice.requests.service.RequestService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(RequestController.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RequestService requestService;

    @Test
    void shouldAddRequest() throws Exception {
        long userId = 1L;
        long eventId = 2L;
        ParticipationRequestDto requestDto = makeParticipationRequestDto(3L, eventId, userId, RequestStatus.PENDING);

        Mockito.when(requestService.addRequest(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(requestDto);

        mockMvc.perform(post("/users/{userId}/requests", userId)
                        .param("eventId", String.valueOf(eventId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(requestDto.getId()))
                .andExpect(jsonPath("$.event").value(eventId))
                .andExpect(jsonPath("$.requester").value(userId))
                .andExpect(jsonPath("$.status").value(RequestStatus.PENDING.toString()))
                .andExpect(jsonPath("$.created").value(requestDto.getCreated()));

        Mockito.verify(requestService).addRequest(userId, eventId);
    }

    @Test
    void shouldThrowConflictWhenAddRequestConditionsAreNotMet() throws Exception {
        Mockito.when(requestService.addRequest(Mockito.anyLong(), Mockito.anyLong()))
                .thenThrow(new EntitiesConflictException("The limit of requests for the event has been reached"));

        mockMvc.perform(post("/users/{userId}/requests", 1L)
                        .param("eventId", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("409 CONFLICT"))
                .andExpect(jsonPath("$.reason").value("For the requested operation the conditions are not met."))
                .andExpect(jsonPath("$.message").value("The limit of requests for the event has been reached"))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(requestService).addRequest(1L, 2L);
    }

    @Test
    void shouldThrowNotFoundWhenAddRequestUserOrEventDoesNotExist() throws Exception {
        Mockito.when(requestService.addRequest(Mockito.anyLong(), Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException("Event with id=2 was not found"));

        mockMvc.perform(post("/users/{userId}/requests", 1L)
                        .param("eventId", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404 NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("The required object was not found."))
                .andExpect(jsonPath("$.message").value("Event with id=2 was not found"))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(requestService).addRequest(1L, 2L);
    }

    @Test
    void shouldGetUserRequests() throws Exception {
        long userId = 1L;
        ParticipationRequestDto request1 = makeParticipationRequestDto(1L, 2L, userId, RequestStatus.PENDING);
        ParticipationRequestDto request2 = makeParticipationRequestDto(2L, 3L, userId, RequestStatus.CONFIRMED);

        Mockito.when(requestService.getUserRequests(Mockito.anyLong()))
                .thenReturn(List.of(request1, request2));

        mockMvc.perform(get("/users/{userId}/requests", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(request1.getId()))
                .andExpect(jsonPath("$[0].event").value(request1.getEvent()))
                .andExpect(jsonPath("$[0].requester").value(userId))
                .andExpect(jsonPath("$[0].status").value(RequestStatus.PENDING.toString()))
                .andExpect(jsonPath("$[1].id").value(request2.getId()))
                .andExpect(jsonPath("$[1].event").value(request2.getEvent()))
                .andExpect(jsonPath("$[1].requester").value(userId))
                .andExpect(jsonPath("$[1].status").value(RequestStatus.CONFIRMED.toString()));

        Mockito.verify(requestService).getUserRequests(userId);
    }

    @Test
    void shouldGetEmptyUserRequests() throws Exception {
        Mockito.when(requestService.getUserRequests(Mockito.anyLong()))
                .thenReturn(List.of());

        mockMvc.perform(get("/users/{userId}/requests", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(requestService).getUserRequests(1L);
    }

    @Test
    void shouldThrowNotFoundWhenGetUserRequestsUserDoesNotExist() throws Exception {
        Mockito.when(requestService.getUserRequests(Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException("User with id=1 was not found"));

        mockMvc.perform(get("/users/{userId}/requests", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404 NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("The required object was not found."))
                .andExpect(jsonPath("$.message").value("User with id=1 was not found"))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(requestService).getUserRequests(1L);
    }

    @Test
    void shouldCancelRequest() throws Exception {
        long userId = 1L;
        long requestId = 2L;
        ParticipationRequestDto requestDto = makeParticipationRequestDto(
                requestId,
                3L,
                userId,
                RequestStatus.CANCELED);

        Mockito.when(requestService.canceledRequest(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(requestDto);

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.event").value(requestDto.getEvent()))
                .andExpect(jsonPath("$.requester").value(userId))
                .andExpect(jsonPath("$.status").value(RequestStatus.CANCELED.toString()))
                .andExpect(jsonPath("$.created").value(requestDto.getCreated()));

        Mockito.verify(requestService).canceledRequest(userId, requestId);
    }

    @Test
    void shouldThrowNotFoundWhenCancelRequestDoesNotExist() throws Exception {
        Mockito.when(requestService.canceledRequest(Mockito.anyLong(), Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException("Request with id=2 was not found"));

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 1L, 2L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404 NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("The required object was not found."))
                .andExpect(jsonPath("$.message").value(containsString("Request with id=2")))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(requestService).canceledRequest(1L, 2L);
    }

    private ParticipationRequestDto makeParticipationRequestDto(long id,
                                                                long eventId,
                                                                long requesterId,
                                                                RequestStatus status) {
        return ParticipationRequestDto.builder()
                .id(id)
                .event(eventId)
                .requester(requesterId)
                .created(EventTestData.FIXED_TIME.format(EventTestData.FORMATTER))
                .status(status)
                .build();
    }
}
