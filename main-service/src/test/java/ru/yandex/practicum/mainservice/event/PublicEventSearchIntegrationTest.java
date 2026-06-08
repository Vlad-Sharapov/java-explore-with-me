package ru.yandex.practicum.mainservice.event;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.mainservice.EventTestData;
import ru.yandex.practicum.mainservice.event.dto.NewEventDto;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.requests.enums.RequestStatus;
import ru.yandex.practicum.mainservice.support.IntegrationTestSupport;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PublicEventSearchIntegrationTest extends IntegrationTestSupport {

    @Test
    void shouldFilterPublishedEventsByTextCategoryAndPaid() throws Exception {
        long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long category1 = createCategory("art");
        Long category2 = createCategory("travel");
        Location location = Location.builder()
                .lat(50.0)
                .lon(40.0)
                .build();
        NewEventDto event1 = EventTestData.makeEventDto(category1, location)
                .paid(true)
                .title("exhibition")
                .eventDate(LocalDateTime.now().plusHours(3))
                .build();
        NewEventDto eventWithDiffCategory = EventTestData.makeEventDto(category2, location)
                .paid(true)
                .title("exhibition")
                .eventDate(LocalDateTime.now().plusHours(3))
                .build();
        NewEventDto eventWithPaidFalse = EventTestData.makeEventDto(category1, location)
                .paid(false)
                .title("exhibition")
                .eventDate(LocalDateTime.now().plusHours(3))
                .build();
        NewEventDto eventWithAnotherText = EventTestData.makeEventDto(category1, location)
                .paid(true)
                .title("Title")
                .eventDate(LocalDateTime.now().plusHours(3))
                .build();

        Long eventId = createEvent(initiatorId, event1);
        Long eventId2 = createEvent(initiatorId, eventWithDiffCategory);
        Long eventId3 = createEvent(initiatorId, eventWithPaidFalse);
        createEvent(initiatorId, eventWithAnotherText);
        publishEvent(eventId);
        publishEvent(eventId2);
        publishEvent(eventId3);


        mockMvc.perform(get("/events")
                        .param("categories", category1.toString())
                        .param("text", "hibi")
                        .param("paid", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(eventId))
                .andExpect(jsonPath("$[0].title").value(event1.getTitle()));
    }

    @Test
    void shouldFilterEventsByDateRange() throws Exception {
        LocalDateTime baseTime = LocalDateTime.now()
                .plusDays(10)
                .withNano(0);

        long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long category1 = createCategory("art");
        Long category2 = createCategory("travel");
        Location location = Location.builder()
                .lat(50.0)
                .lon(40.0)
                .build();
        NewEventDto beforeEvent = EventTestData.makeEventDto(category1, location)
                .eventDate(baseTime.minusDays(2))
                .build();
        NewEventDto insideEvent = EventTestData.makeEventDto(category2, location)
                .eventDate(baseTime.plusDays(2))
                .build();
        NewEventDto afterEvent = EventTestData.makeEventDto(category1, location)
                .eventDate(baseTime.plusDays(6))
                .build();

        Long eventId = createEvent(initiatorId, beforeEvent);
        Long eventId2 = createEvent(initiatorId, insideEvent);
        Long eventId3 = createEvent(initiatorId, afterEvent);
        publishEvent(eventId);
        publishEvent(eventId2);
        publishEvent(eventId3);


        mockMvc.perform(get("/events")
                        .param("rangeStart", baseTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("rangeEnd", baseTime.plusDays(4)
                                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(eventId2))
                .andExpect(jsonPath("$[0].title").value(insideEvent.getTitle()));

    }

    @Test
    void shouldReturnBadRequestWhenBadDateInterval() throws Exception {
        LocalDateTime baseTime = LocalDateTime.now()
                .plusDays(10)
                .withNano(0);

        long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long category1 = createCategory("art");
        Location location = Location.builder()
                .lat(50.0)
                .lon(40.0)
                .build();
        NewEventDto beforeEvent = EventTestData.makeEventDto(category1, location)
                .eventDate(baseTime.minusDays(2))
                .build();

        Long eventId = createEvent(initiatorId, beforeEvent);
        publishEvent(eventId);


        mockMvc.perform(get("/events")
                        .param("rangeEnd", baseTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("rangeStart", baseTime.plusDays(4)
                                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.message").value("Fields: start, end. Error: The start time should not be later than the end time"))
                .andExpect(jsonPath("$.timestamp").exists());
    }


    @Test
    void shouldSortByEventDateAndApplyPagination() throws Exception {
        LocalDateTime baseTime = LocalDateTime.now()
                .plusDays(10)
                .withNano(0);

        long initiatorId = createUser("initiator", "initiator@mail.ru");
        Long category1 = createCategory("art");
        Long category2 = createCategory("travel");
        Location location = Location.builder()
                .lat(50.0)
                .lon(40.0)
                .build();
        NewEventDto beforeEvent = EventTestData.makeEventDto(category1, location)
                .eventDate(baseTime.minusDays(2))
                .build();
        NewEventDto insideEvent = EventTestData.makeEventDto(category2, location)
                .eventDate(baseTime.plusDays(2))
                .build();
        NewEventDto afterEvent = EventTestData.makeEventDto(category1, location)
                .eventDate(baseTime.plusDays(6))
                .build();

        Long eventId = createEvent(initiatorId, beforeEvent);
        Long eventId2 = createEvent(initiatorId, insideEvent);
        Long eventId3 = createEvent(initiatorId, afterEvent);
        publishEvent(eventId);
        publishEvent(eventId2);
        publishEvent(eventId3);


        mockMvc.perform(get("/events")
                        .param("sort", "event_date")
                        .param("size", "1"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(eventId))
                .andExpect(jsonPath("$[0].title").value(beforeEvent.getTitle()));
    }

    @Test
    void shouldReturnOnlyEventsWithAvailablePlaces() throws Exception {
        LocalDateTime baseTime = LocalDateTime.now()
                .plusDays(10)
                .withNano(0);

        long initiatorId = createUser("initiator", "initiator@mail.ru");
        long requester1Id = createUser("requester1", "requester1@mail.ru");
        long requester2Id = createUser("requester2", "requester2@mail.ru");
        Long category1 = createCategory("art");
        Long category2 = createCategory("travel");
        Location location = Location.builder()
                .lat(50.0)
                .lon(40.0)
                .build();
        NewEventDto event1 = EventTestData.makeEventDto(category1, location)
                .eventDate(baseTime.minusDays(2))
                .build();
        NewEventDto event2 = EventTestData.makeEventDto(category2, location)
                .eventDate(baseTime.plusDays(2))
                .participantLimit(1)
                .build();
        NewEventDto event3 = EventTestData.makeEventDto(category1, location)
                .eventDate(baseTime.plusDays(6))
                .participantLimit(1)
                .build();

        Long eventId = createEvent(initiatorId, event1);
        Long eventId2 = createEvent(initiatorId, event2);
        Long eventId3 = createEvent(initiatorId, event3);

        publishEvent(eventId);
        publishEvent(eventId2);
        publishEvent(eventId3);

        Long request1 = createRequest(requester1Id, eventId2, RequestStatus.PENDING);
        Long request2 = createRequest(requester2Id, eventId2, RequestStatus.PENDING);

        confirmOrRejectRequest(initiatorId, eventId2, request1, RequestStatus.CONFIRMED);
        confirmOrRejectRequest(initiatorId, eventId3, request2, RequestStatus.CONFIRMED);

        mockMvc.perform(get("/events")
                        .param("sort", "event_date")
                        .param("size", "1"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(eventId))
                .andExpect(jsonPath("$[0].title").value(event1.getTitle()));
    }
}
