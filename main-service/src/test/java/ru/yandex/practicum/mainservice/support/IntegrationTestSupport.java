package ru.yandex.practicum.mainservice.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.mainservice.EventTestData;
import ru.yandex.practicum.mainservice.TestUtils;
import ru.yandex.practicum.mainservice.category.dto.CategoryDto;
import ru.yandex.practicum.mainservice.event.dto.NewEventDto;
import ru.yandex.practicum.mainservice.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.enums.adminenum.AdmStateAction;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.event.stateclient.ClientHandler;
import ru.yandex.practicum.mainservice.requests.enums.RequestStatus;
import ru.yandex.practicum.mainservice.requests.model.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.mainservice.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected ClientHandler clientHandler;

    @BeforeEach
    protected void setUpStatisticsClient() {
        Mockito.when(clientHandler.getStatsForEvents(
                        Mockito.anyCollection(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of());

        Mockito.when(clientHandler.getStatsForEvent(
                        Mockito.any(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of());
    }

    protected Long createUser(String name, String email) throws Exception {
        UserDto user = UserDto.builder()
                .name(name)
                .email(email)
                .build();

        MvcResult result = mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(objectMapper, user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andReturn();

        return TestUtils.extractId(objectMapper, result);
    }

    protected Long createCategory(String name) throws Exception {
        CategoryDto category = CategoryDto.builder()
                .name(name)
                .build();

        MvcResult result = mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(objectMapper, category)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andReturn();

        return TestUtils.extractId(objectMapper, result);
    }

    protected Long createEvent(Long initiatorId,
                               Long categoryId,
                               boolean requestModeration,
                               int participantLimit) throws Exception {
        Location location = Location.builder()
                .lat(50.0)
                .lon(45.0)
                .build();
        return createEvent(initiatorId, categoryId, requestModeration, participantLimit, location);
    }

    protected Long createEvent(Long initiatorId,
                               Long categoryId,
                               boolean requestModeration,
                               int participantLimit,
                               Location location) throws Exception {


        NewEventDto event = EventTestData.makeEventDto(categoryId, location)
                .requestModeration(requestModeration)
                .participantLimit(participantLimit)
                .eventDate(LocalDateTime.now().plusHours(3))
                .build();

        return createEvent(initiatorId, event);
    }

    protected Long createEvent(Long initiatorId, NewEventDto eventDto) throws Exception {

        MvcResult result = mockMvc.perform(post("/users/{userId}/events", initiatorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(objectMapper, eventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.initiator.id").value(initiatorId))
                .andExpect(jsonPath("$.state").value(EventState.PENDING.toString()))
                .andReturn();

        return TestUtils.extractId(objectMapper, result);
    }

    protected void publishEvent(Long eventId) throws Exception {
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdmStateAction.PUBLISH_EVENT)
                .build();

        mockMvc.perform(patch("/admin/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(objectMapper, request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(EventState.PUBLISHED.toString()));
    }

    protected Long createRequest(Long requesterId,
                                 Long eventId,
                                 RequestStatus expectedStatus) throws Exception {
        MvcResult result = mockMvc.perform(post("/users/{userId}/requests", requesterId)
                        .param("eventId", eventId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requester").value(requesterId))
                .andExpect(jsonPath("$.event").value(eventId))
                .andExpect(jsonPath("$.status").value(expectedStatus.toString()))
                .andReturn();

        return TestUtils.extractId(objectMapper, result);
    }

    protected void confirmOrRejectRequest(Long initiatorId, Long eventId, Long requestId, RequestStatus requestStatus) throws Exception {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(requestId));
        updateRequest.setStatus(requestStatus);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", initiatorId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(objectMapper, updateRequest)))
                .andExpect(status().isOk());
    }
}
