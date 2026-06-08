package ru.yandex.practicum.mainservice.requests;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.mainservice.requests.enums.RequestStatus;
import ru.yandex.practicum.mainservice.support.IntegrationTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RequestIntegrationTest extends IntegrationTestSupport {

    @Test
    void shouldAddConfirmedRequestWhenEventDoesNotRequireModeration() throws Exception {
        Long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long requesterId = createUser("requester", "requester@mail.ru");
        Long categoryId = createCategory("category");
        Long eventId = createEvent(initiatorId, categoryId, false, 10);
        publishEvent(eventId);

        createRequest(requesterId, eventId, RequestStatus.CONFIRMED);
    }

    @Test
    void shouldReturnConflictWhenEventDoesNotRequireModerationAndLimitReached() throws Exception {
        Long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long requesterId = createUser("requester", "requester@mail.ru");
        Long requester2Id = createUser("requester2", "requester2@mail.ru");
        Long categoryId = createCategory("category");
        Long eventId = createEvent(initiatorId, categoryId, false, 1);
        publishEvent(eventId);

        createRequest(requesterId, eventId, RequestStatus.CONFIRMED);
        mockMvc.perform(post("/users/{userId}/requests", requester2Id)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldAddRequestAndConfirmIt() throws Exception {
        Long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long requesterId = createUser("requester", "requester@mail.ru");
        Long categoryId = createCategory("category");
        Long eventId = createEvent(initiatorId, categoryId, true, 10);
        publishEvent(eventId);

        Long requestId = createRequest(requesterId, eventId, RequestStatus.PENDING);
        confirmOrRejectRequest(initiatorId, eventId, requestId, RequestStatus.CONFIRMED);

        mockMvc.perform(get("/users/{userId}/requests", requesterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestId))
                .andExpect(jsonPath("$[0].requester").value(requesterId))
                .andExpect(jsonPath("$[0].event").value(eventId))
                .andExpect(jsonPath("$[0].status").value(RequestStatus.CONFIRMED.toString()));
    }

    @Test
    void shouldAddRequestAndRejectIt() throws Exception {
        Long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long requesterId = createUser("requester", "requester@mail.ru");
        Long categoryId = createCategory("category");
        Long eventId = createEvent(initiatorId, categoryId, true, 10);
        publishEvent(eventId);

        Long requestId = createRequest(requesterId, eventId, RequestStatus.PENDING);
        confirmOrRejectRequest(initiatorId, eventId, requestId, RequestStatus.REJECTED);

        mockMvc.perform(get("/users/{userId}/requests", requesterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestId))
                .andExpect(jsonPath("$[0].requester").value(requesterId))
                .andExpect(jsonPath("$[0].event").value(eventId))
                .andExpect(jsonPath("$[0].status").value(RequestStatus.REJECTED.toString()));
    }

    @Test
    void shouldReturnConflictWhenInitiatorCreatesRequest() throws Exception {
        Long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long categoryId = createCategory("category");
        Long eventId = createEvent(initiatorId, categoryId, true, 10);
        publishEvent(eventId);

        mockMvc.perform(post("/users/{userId}/requests", initiatorId)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnConflictWhenParticipantLimitReached() throws Exception {
        Long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long requester1Id = createUser("firstRequester", "first@mail.ru");
        Long requester2Id = createUser("secondRequester", "second@mail.ru");
        Long categoryId = createCategory("category");
        Long eventId = createEvent(initiatorId, categoryId, true, 1);
        publishEvent(eventId);

        Long requestId = createRequest(requester1Id, eventId, RequestStatus.PENDING);
        confirmOrRejectRequest(initiatorId, eventId, requestId, RequestStatus.CONFIRMED);

        mockMvc.perform(post("/users/{userId}/requests", requester2Id)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isConflict());
    }


    @Test
    void shouldReturnConflictWhenRequesterCreateTwoRequestsForEvent() throws Exception {
        Long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long requester1Id = createUser("firstRequester", "first@mail.ru");
        Long requester2Id = createUser("secondRequester", "second@mail.ru");
        Long categoryId = createCategory("category");
        Long eventId = createEvent(initiatorId, categoryId, true, 1);
        publishEvent(eventId);

        Long requestId = createRequest(requester1Id, eventId, RequestStatus.PENDING);
        confirmOrRejectRequest(initiatorId, eventId, requestId, RequestStatus.CONFIRMED);

        mockMvc.perform(post("/users/{userId}/requests", requester2Id)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnConflictWhenRequestCancelAnotherUser() throws Exception {
        Long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long requester1Id = createUser("firstRequester", "first@mail.ru");
        Long requester2Id = createUser("secondRequester", "second@mail.ru");
        Long categoryId = createCategory("category");
        Long eventId = createEvent(initiatorId, categoryId, true, 1);
        publishEvent(eventId);

        Long requestId = createRequest(requester1Id, eventId, RequestStatus.PENDING);

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", requester2Id, requestId)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnConflictWhenRequestCancelAfterConfirmed() throws Exception {
        Long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long requester1Id = createUser("firstRequester", "first@mail.ru");
        Long requester2Id = createUser("secondRequester", "second@mail.ru");
        Long categoryId = createCategory("category");
        Long eventId = createEvent(initiatorId, categoryId, true, 1);
        publishEvent(eventId);

        Long requestId = createRequest(requester1Id, eventId, RequestStatus.PENDING);

        confirmOrRejectRequest(initiatorId, eventId, requestId, RequestStatus.CONFIRMED);

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", requester1Id, requestId)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isConflict());

    }

}
