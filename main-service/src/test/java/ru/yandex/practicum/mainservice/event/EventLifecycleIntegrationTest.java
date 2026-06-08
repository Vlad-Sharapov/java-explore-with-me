package ru.yandex.practicum.mainservice.event;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.yandex.practicum.mainservice.TestUtils;
import ru.yandex.practicum.mainservice.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.enums.userenum.UsrStateAction;
import ru.yandex.practicum.mainservice.support.IntegrationTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventLifecycleIntegrationTest extends IntegrationTestSupport {

    @Test
    void shouldCreateCancelSendToReviewPublishAndReturnEvent() throws Exception {
        Long userId = createUser("testName", "test@mail.ru");
        Long categoryId = createCategory("category");
        Long eventId = createEvent(userId, categoryId, true, 10);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/events/{eventId}", eventId))
                .andExpect(status().isNotFound());

        UpdateEventUserRequest cancelRequest = UpdateEventUserRequest.builder()
                .title("New title")
                .annotation("New valid annotation text")
                .stateAction(UsrStateAction.CANCEL_REVIEW)
                .build();

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .content(TestUtils.asJsonString(objectMapper, cancelRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.annotation").value("New valid annotation text"))
                .andExpect(jsonPath("$.state").value(EventState.CANCELED.toString()));

        UpdateEventUserRequest sendToReviewRequest = UpdateEventUserRequest.builder()
                .stateAction(UsrStateAction.SEND_TO_REVIEW)
                .build();

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .content(TestUtils.asJsonString(objectMapper, sendToReviewRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.annotation").value("New valid annotation text"))
                .andExpect(jsonPath("$.state").value(EventState.PENDING.toString()));

        publishEvent(eventId);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/events/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.state").value(EventState.PUBLISHED.toString()))
                .andExpect(jsonPath("$.initiator.id").value(userId));
    }

    @Test
    void shouldReturnConflictWhenUserCancelsPublishedEvent() throws Exception {
        Long userId = createUser("testName", "testEmail@mail.ru");
        Long categoryId = createCategory("category");
        Long eventId = createEvent(userId, categoryId, true, 10);
        publishEvent(eventId);

        UpdateEventUserRequest cancelRequest = UpdateEventUserRequest.builder()
                .stateAction(UsrStateAction.CANCEL_REVIEW)
                .build();

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", userId, eventId)
                        .content(TestUtils.asJsonString(objectMapper, cancelRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}
