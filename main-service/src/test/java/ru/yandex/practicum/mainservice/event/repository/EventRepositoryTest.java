package ru.yandex.practicum.mainservice.event.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.event.model.QEvent;
import ru.yandex.practicum.mainservice.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@DataJpaTest
class EventRepositoryTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 1, 10, 12, 0);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldFilterPublicEventsByCategoryPaidRangeAndState() {
        User initiator = persistUser("user1");
        Category targetCategory = persistCategory("concerts");
        Category otherCategory = persistCategory("lectures");

        Event matchedEvent = persistEvent("Matched event", initiator, targetCategory, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        persistEvent("Pending event", initiator, targetCategory, false,
                EventState.PENDING, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        persistEvent("Other category event", initiator, otherCategory, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        persistEvent("Paid event", initiator, targetCategory, true,
                EventState.PUBLISHED, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        persistEvent("Out of range event", initiator, targetCategory, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(5), persistLocation(55.75, 37.61));
        flushAndClear();

        QEvent event = QEvent.event;
        BooleanExpression condition = event.state.eq(EventState.PUBLISHED)
                .and(event.category.id.eq(targetCategory.getId()))
                .and(event.paid.eq(false))
                .and(event.eventDate.between(FIXED_TIME, FIXED_TIME.plusDays(2)));

        List<Event> result = eventRepository.findAll(condition, PageRequest.of(0, 10))
                .getContent();

        assertThat(result, hasSize(1));
        assertThat("Matched event", equalTo(matchedEvent.getTitle()));
    }

    @Test
    void shouldFindPublicEventsByTextInTitleAnnotationOrDescription() {
        User initiator = persistUser("user1");
        Category category = persistCategory("concerts");

        Event eventWithTextInTitle = persistEvent("Gorbuffet title", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        Event eventWithTextInAnnotation = persistEvent("Title without match", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        eventWithTextInAnnotation.setAnnotation("Annotation with gorbuffet keyword");
        Event eventWithTextInDescription = persistEvent("Another title", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        eventWithTextInDescription.setDescription("Description with GORBUFFET keyword");
        persistEvent("No match", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        persistEvent("Unpublished Gorbuffet event", initiator, category, false,
                EventState.PENDING, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        flushAndClear();

        QEvent event = QEvent.event;
        BooleanExpression textCondition = event.annotation.likeIgnoreCase("%gorbuffet%")
                .or(event.description.likeIgnoreCase("%gorbuffet%"))
                .or(event.title.likeIgnoreCase("%gorbuffet%"));
        BooleanExpression condition = event.state.eq(EventState.PUBLISHED)
                .and(textCondition);

        Page<Event> result = eventRepository.findAll(condition, PageRequest.of(0, 10));

        assertThat(result.getContent(), hasSize(3));
        assertThat(getEventIds(result), containsInAnyOrder(
                eventWithTextInTitle.getId(),
                eventWithTextInAnnotation.getId(),
                eventWithTextInDescription.getId()
        ));
    }

    @Test
    void findByIdAndState() {
        User initiator = persistUser("user1");
        Category category = persistCategory("concerts");

        Event event1 = persistEvent("title", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        persistEvent("Title without match", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        persistEvent("Another title", initiator, category, false,
                EventState.PENDING, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        persistEvent("No match", initiator, category, false,
                EventState.PENDING, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        persistEvent("Unpublished  event", initiator, category, false,
                EventState.PENDING, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        flushAndClear();

        Event result = eventRepository.findByIdAndState(event1.getId(), EventState.PUBLISHED).get();

        assertThat(result.getTitle(), equalTo("title"));
        assertThat(result.getAnnotation(), equalTo("Default annotation for title"));
        assertThat(result.getInitiator().getId(), equalTo(initiator.getId()));
        assertThat(result.getCategory().getId(), equalTo(category.getId()));
    }

    @Test
    void findAllByIdAsSet() {

        User initiator = persistUser("user1");
        Category category = persistCategory("concerts");

        Event event1 = persistEvent("title", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        Event event2 = persistEvent("Title without match", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(1), persistLocation(55.74, 37.61));
        persistEvent("Another title", initiator, category, false,
                EventState.PENDING, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        persistEvent("No match", initiator, category, false,
                EventState.PENDING, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        persistEvent("Unpublished  event", initiator, category, false,
                EventState.PENDING, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        flushAndClear();

        Set<Event> result = eventRepository.findAllByIdAsSet(List.of(event1.getId(), event2.getId()));

        assertThat(result.size(), equalTo(2));
        assertThat(event1.getId(), equalTo(event1.getId()));
        assertThat(event2.getId(), equalTo(event2.getId()));
        assertThat(event1.getTitle(), equalTo("title"));
        assertThat(event2.getTitle(), equalTo("Title without match"));
    }

    @Test
    void findEventsByLocation() {
        User initiator = persistUser("user1");
        Category category = persistCategory("concerts");

        Event event1 = persistEvent("title", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        Event event2 = persistEvent("Title without match", initiator, category, false,
                EventState.PENDING, FIXED_TIME.plusDays(1), persistLocation(55.74, 37.61));
        persistEvent("Another title", initiator, category, false,
                EventState.PENDING, FIXED_TIME.plusDays(1), persistLocation(12.75, 37.61));
        persistEvent("No match", initiator, category, false,
                EventState.PENDING, FIXED_TIME.plusDays(1), persistLocation(12.75, 37.61));
        persistEvent("Unpublished  event", initiator, category, false,
                EventState.PENDING, FIXED_TIME.plusDays(1), persistLocation(70.75, 37.61));
        flushAndClear();

        List<Event> result = eventRepository.findEventsByLocation(55.75, 37.61, 1000.0);

        assertThat(result.size(), equalTo(2));
        assertThat(result.get(0).getTitle(), equalTo(event1.getTitle()));
        assertThat(result.get(1).getTitle(), equalTo(event2.getTitle()));

    }

    @Test
    void findPublishedEventsByLocation() {
        User initiator = persistUser("user1");
        Category category = persistCategory("concerts");

        Event event1 = persistEvent("title", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(1), persistLocation(55.75, 37.61));
        Event event2 = persistEvent("Title without match", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(1), persistLocation(55.74, 37.61));
        persistEvent("Another title", initiator, category, false,
                EventState.PENDING, FIXED_TIME.plusDays(1), persistLocation(12.75, 37.61));
        persistEvent("No match", initiator, category, false,
                EventState.PENDING, FIXED_TIME.plusDays(1), persistLocation(12.75, 37.61));
        persistEvent("Unpublished  event", initiator, category, false,
                EventState.PENDING, FIXED_TIME.plusDays(1), persistLocation(70.75, 37.61));
        flushAndClear();

        List<Event> result = eventRepository.findPublishedEventsByLocation(55.75, 37.61, 1000.0);

        assertThat(result.size(), equalTo(2));
        assertThat(result.get(0).getTitle(), equalTo(event1.getTitle()));
        assertThat(result.get(1).getTitle(), equalTo(event2.getTitle()));

    }


    private User persistUser(String suffix) {
        User user = User.builder()
                .name("User " + suffix)
                .email(suffix + "@mail.ru")
                .build();
        entityManager.persist(user);
        return user;
    }

    private Category persistCategory(String name) {
        Category category = Category.builder()
                .name(name)
                .build();
        entityManager.persist(category);
        return category;
    }

    private Event persistEvent(String title,
                               User initiator,
                               Category category,
                               boolean paid,
                               EventState state,
                               LocalDateTime eventDate,
                               Location location) {
        Event event = Event.builder()
                .initiator(initiator)
                .category(category)
                .location(location)
                .annotation("Default annotation for " + title)
                .description("Default description for " + title)
                .title(title)
                .createOn(FIXED_TIME)
                .publishedOn(FIXED_TIME.plusHours(1))
                .eventDate(eventDate)
                .paid(paid)
                .participantLimit(10)
                .requestModeration(true)
                .state(state)
                .build();
        entityManager.persist(event);
        return event;
    }

    private Location persistLocation(double lat, double lon) {
        Location location = Location.builder()
                .lat(lat)
                .lon(lon)
                .build();
        entityManager.persist(location);
        return location;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private List<Long> getEventIds(Page<Event> events) {
        return events.getContent().stream()
                .map(Event::getId)
                .toList();
    }
}
