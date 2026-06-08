package ru.yandex.practicum.mainservice.compilation.service.publics.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.mainservice.CompilationTestData;
import ru.yandex.practicum.mainservice.EventTestData;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.compilation.dto.CompilationDto;
import ru.yandex.practicum.mainservice.compilation.model.Compilation;
import ru.yandex.practicum.mainservice.compilation.repository.CompilationRepository;
import ru.yandex.practicum.mainservice.event.dto.EventShortDto;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.event.stateclient.ClientHandler;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.requests.enums.RequestStatus;
import ru.yandex.practicum.mainservice.requests.model.Request;
import ru.yandex.practicum.mainservice.requests.repository.RequestRepository;
import ru.yandex.practicum.mainservice.user.model.User;
import ru.yandex.practicum.statdto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
class CompilationPublicServiceImplTest {

    @InjectMocks
    private CompilationPublicServiceImpl compilationPublicService;

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private ClientHandler clientHandler;

    @Test
    void getCompilations() {
        User user1 = EventTestData.makeUser(1L);
        User user2 = EventTestData.makeUser(2L);
        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);
        Category category = EventTestData.makeCategory(1L);
        Event event1 = EventTestData.makeEvent(user1, category, location)
                .build();

        Event event2 = EventTestData.makeEvent(user2, category, location)
                .id(2L)
                .title("title2")
                .build();

        Compilation compilation1 = CompilationTestData.makeNewCompilation(Set.of(event1, event2))
                .build();

        Compilation compilation2 = CompilationTestData.makeNewCompilation(Set.of(event1))
                .id(2L)
                .title("title1")
                .build();

        Request request = EventTestData.makeRequest(1L, event1, user2)
                .status(RequestStatus.CONFIRMED)
                .build();

        Mockito.when(compilationRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(compilation1, compilation2)));

        Mockito.when(clientHandler.getStatsForEvents(Mockito.anyCollection(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of(
                        StatsDto.builder()
                                .uri("/events/" + event1.getId())
                                .hits(5L)
                                .app("ewm")
                                .build(),
                        StatsDto.builder()
                                .uri("/events/" + event2.getId())
                                .hits(3L)
                                .app("ewm")
                                .build()
                ));

        Mockito.when(requestRepository.findAllByStatusAndEventIn(Mockito.eq(RequestStatus.CONFIRMED),
                        Mockito.anyCollection()))
                .thenReturn(List.of(request));

        List<CompilationDto> result = compilationPublicService.getCompilations(false, 0, 10);

        Mockito.verify(compilationRepository, Mockito.never()).findAll(
                Mockito.any(PageRequest.class));

        assertThat(result.size(), equalTo(2));
        assertThat(result.stream()
                .map(CompilationDto::getId)
                .toList(), containsInAnyOrder(compilation1.getId(), compilation2.getId()));

        List<Long> eventsIds = result.stream()
                .map(CompilationDto::getEvents)
                .flatMap(events -> events.stream()
                        .map(EventShortDto::getId))
                .toList();

        assertThat(eventsIds, containsInAnyOrder(event1.getId(), event1.getId(), event2.getId()));

        CompilationDto compilationWithTwoEvents = result.stream()
                .filter(compilationDto -> compilationDto.getId() == compilation1.getId())
                .findFirst().orElseThrow(AssertionError::new);
        CompilationDto compilationWithOneEvent = result.stream()
                .filter(compilationDto -> compilationDto.getId() == compilation2.getId())
                .findFirst().orElseThrow(AssertionError::new);

        Map<Long, Long> viewsByEventId = compilationWithTwoEvents.getEvents().stream()
                .collect(Collectors.toMap(EventShortDto::getId, EventShortDto::getViews));
        Map<Long, Long> confirmedRequestsByEventId = compilationWithTwoEvents.getEvents().stream()
                .collect(Collectors.toMap(EventShortDto::getId, EventShortDto::getConfirmedRequests));

        assertThat(viewsByEventId.get(event1.getId()), equalTo(5L));
        assertThat(viewsByEventId.get(event2.getId()), equalTo(3L));
        assertThat(confirmedRequestsByEventId.get(event1.getId()), equalTo(1L));
        assertThat(confirmedRequestsByEventId.get(event2.getId()), equalTo(0L));
        assertThat(compilationWithOneEvent.getEvents().stream()
                .map(EventShortDto::getId)
                .toList(), containsInAnyOrder(event1.getId()));
    }

    @Test
    void shouldGetCompilationsWhenPinnedIsNull() throws Exception {
        User user1 = EventTestData.makeUser(1L);
        User user2 = EventTestData.makeUser(2L);
        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);
        Category category = EventTestData.makeCategory(1L);
        Event event1 = EventTestData.makeEvent(user1, category, location)
                .build();

        Event event2 = EventTestData.makeEvent(user2, category, location)
                .id(2L)
                .title("title2")
                .build();

        Compilation compilation1 = CompilationTestData.makeNewCompilation(Set.of(event1, event2))
                .pinned(false)
                .build();

        Request request = EventTestData.makeRequest(1L, event1, user2)
                .status(RequestStatus.CONFIRMED)
                .build();


        Mockito.when(clientHandler.getStatsForEvents(Mockito.anyCollection(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of(
                        StatsDto.builder()
                                .uri("/events/" + event1.getId())
                                .hits(5L)
                                .app("ewm")
                                .build(),
                        StatsDto.builder()
                                .uri("/events/" + event2.getId())
                                .hits(3L)
                                .app("ewm")
                                .build()
                ));

        Mockito.when(compilationRepository.findAll(Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(compilation1)));

        Mockito.when(requestRepository.findAllByStatusAndEventIn(Mockito.eq(RequestStatus.CONFIRMED),
                        Mockito.anyCollection()))
                .thenReturn(List.of(request));

        List<CompilationDto> result = compilationPublicService.getCompilations(null, 0, 10);

        Mockito.verify(compilationRepository, Mockito.never()).findAll(Mockito.any(BooleanExpression.class),
                Mockito.any(PageRequest.class));


        assertThat(result.size(), equalTo(1));
        assertThat(result.get(0).getId(), equalTo(compilation1.getId()));
        assertThat(result.get(0).getTitle(), equalTo(compilation1.getTitle()));

        List<Long> eventsIds = result.stream()
                .map(CompilationDto::getEvents)
                .flatMap(events -> events.stream()
                        .map(EventShortDto::getId))
                .toList();

        assertThat(eventsIds, containsInAnyOrder(event1.getId(), event2.getId()));
    }


    @Test
    void shouldReturnEmptyListWhenNoCompilationsFound() throws Exception {

        Mockito.when(clientHandler.getStatsForEvents(Mockito.anyCollection(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of());

        Mockito.when(compilationRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Mockito.when(requestRepository.findAllByStatusAndEventIn(Mockito.eq(RequestStatus.CONFIRMED),
                        Mockito.anyCollection()))
                .thenReturn(List.of());

        List<CompilationDto> result = compilationPublicService.getCompilations(false, 0, 10);

        Mockito.verify(compilationRepository, Mockito.never()).findAll(
                Mockito.any(PageRequest.class));

        assertThat(result.size(), equalTo(0));

    }

    @Test
    void shouldGetCompilation() {

        User user1 = EventTestData.makeUser(1L);
        User user2 = EventTestData.makeUser(2L);
        Location location = EventTestData.makeLocation(1L, 50.0, 45.0);
        Category category = EventTestData.makeCategory(1L);
        Event event1 = EventTestData.makeEvent(user1, category, location)
                .build();

        Event event2 = EventTestData.makeEvent(user2, category, location)
                .id(2L)
                .title("title2")
                .build();

        Compilation compilation = CompilationTestData.makeNewCompilation(Set.of(event1, event2))
                .build();


        Request request = EventTestData.makeRequest(1L, event1, user2)
                .status(RequestStatus.CONFIRMED)
                .build();

        Mockito.when(compilationRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(compilation));

        Mockito.when(clientHandler.getStatsForEvents(Mockito.anyCollection(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of(
                        StatsDto.builder()
                                .uri("/events/" + event1.getId())
                                .hits(5L)
                                .app("ewm")
                                .build(),
                        StatsDto.builder()
                                .uri("/events/" + event2.getId())
                                .hits(3L)
                                .app("ewm")
                                .build()
                ));

        Mockito.when(requestRepository.findAllByStatusAndEventIn(Mockito.eq(RequestStatus.CONFIRMED),
                        Mockito.anyCollection()))
                .thenReturn(List.of(request));

        CompilationDto result = compilationPublicService.getCompilation(1L);

        List<Long> eventsIds = result.getEvents().stream().map(EventShortDto::getId)
                .toList();
        assertThat(eventsIds, containsInAnyOrder(event1.getId(), event2.getId()));


        Map<Long, Long> viewsByEventId = result.getEvents().stream()
                .collect(Collectors.toMap(EventShortDto::getId, EventShortDto::getViews));
        Map<Long, Long> confirmedRequestsByEventId = result.getEvents().stream()
                .collect(Collectors.toMap(EventShortDto::getId, EventShortDto::getConfirmedRequests));

        assertThat(viewsByEventId.get(event1.getId()), equalTo(5L));
        assertThat(viewsByEventId.get(event2.getId()), equalTo(3L));
        assertThat(confirmedRequestsByEventId.get(event1.getId()), equalTo(1L));
        assertThat(confirmedRequestsByEventId.get(event2.getId()), equalTo(0L));

    }


    @Test
    void shouldThrowExceptionWhenCompilationNotFound() {

        Mockito.when(compilationRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> compilationPublicService.getCompilation(1L));
    }

}
