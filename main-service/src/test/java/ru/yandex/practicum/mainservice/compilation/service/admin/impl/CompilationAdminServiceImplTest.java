package ru.yandex.practicum.mainservice.compilation.service.admin.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mainservice.CompilationTestData;
import ru.yandex.practicum.mainservice.EventTestData;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.compilation.dto.CompilationDto;
import ru.yandex.practicum.mainservice.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.mainservice.compilation.model.Compilation;
import ru.yandex.practicum.mainservice.compilation.repository.CompilationRepository;
import ru.yandex.practicum.mainservice.event.dto.EventShortDto;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.event.repository.EventRepository;
import ru.yandex.practicum.mainservice.event.stateclient.ClientHandler;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.requests.enums.RequestStatus;
import ru.yandex.practicum.mainservice.requests.repository.RequestRepository;
import ru.yandex.practicum.mainservice.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CompilationAdminServiceImplTest {

    @InjectMocks
    private CompilationAdminServiceImpl compilationAdminService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private ClientHandler clientHandler;


    @Test
    void shouldAddNewCompilation() {

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

        NewCompilationDto compilationDto = CompilationTestData
                .makeNewCompilationDto(Set.of(event1.getId(), event2.getId()))
                .build();

        Compilation compilation = CompilationTestData.makeNewCompilation(Set.of(event1, event2))
                .build();

        Mockito.when(eventRepository.findAllByIdAsSet(Mockito.anyCollection()))
                .thenReturn(Set.of(event1, event2));

        Mockito.when(compilationRepository.save(Mockito.any(Compilation.class)))
                .thenAnswer(invocationOnMock -> {
                    Compilation returnCompilation = invocationOnMock.getArgument(0);
                    returnCompilation.setId(compilation.getId());
                    return returnCompilation;
                });

        Mockito.when(clientHandler.getStatsForEvents(Mockito.anyCollection(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of());

        Mockito.when(requestRepository.findAllByStatusAndEventIn(Mockito.eq(RequestStatus.CONFIRMED),
                        Mockito.anyCollection()))
                .thenReturn(List.of());

        CompilationDto result = compilationAdminService.add(compilationDto);

        Mockito.verify(eventRepository).findAllByIdAsSet(Mockito.eq(compilationDto.getEvents()));

        ArgumentCaptor<Compilation> compilationCaptor = ArgumentCaptor.forClass(Compilation.class);
        Mockito.verify(compilationRepository).save(compilationCaptor.capture());
        Compilation capturedCompilation = compilationCaptor.getValue();

        assertThat(capturedCompilation.getId(), equalTo(compilation.getId()));
        assertThat(capturedCompilation.getEvents().size(), equalTo(2));
        assertThat(capturedCompilation.getPinned(), equalTo(compilation.getPinned()));

        assertThat(result.getId(), equalTo(compilation.getId()));
        assertThat(result.getEvents().size(), equalTo(2));
        assertThat(result.getEvents().stream()
                .map(EventShortDto::getId)
                .toList(), containsInAnyOrder(event1.getId(), event2.getId()));

        assertThat(result.getEvents().stream()
                .map(EventShortDto::getTitle)
                .toList(), containsInAnyOrder(event1.getTitle(), event2.getTitle()));


    }

    @Test
    void shouldUpdateCompilation() {

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

        NewCompilationDto compilationDto = CompilationTestData
                .makeNewCompilationDto(Set.of(event1.getId(), event2.getId()))
                .title("Updated title")
                .build();

        Compilation compilation = CompilationTestData.makeNewCompilation(Set.of(event1, event2))
                .title("Updated title")
                .build();

        Mockito.when(compilationRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(compilation));

        Mockito.when(eventRepository.findAllByIdAsSet(Mockito.anyCollection()))
                .thenReturn(Set.of(event1, event2));

        Mockito.when(compilationRepository.save(Mockito.any(Compilation.class)))
                .thenAnswer(invocationOnMock -> {
                    Compilation returnCompilation = invocationOnMock.getArgument(0);
                    returnCompilation.setId(compilation.getId());
                    return returnCompilation;
                });

        Mockito.when(clientHandler.getStatsForEvents(Mockito.anyCollection(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of());

        Mockito.when(requestRepository.findAllByStatusAndEventIn(Mockito.eq(RequestStatus.CONFIRMED),
                        Mockito.anyCollection()))
                .thenReturn(List.of());

        CompilationDto result = compilationAdminService.update(1L, compilationDto);

        Mockito.verify(eventRepository).findAllByIdAsSet(Mockito.eq(compilationDto.getEvents()));

        ArgumentCaptor<Compilation> compilationCaptor = ArgumentCaptor.forClass(Compilation.class);
        Mockito.verify(compilationRepository).save(compilationCaptor.capture());
        Compilation capturedCompilation = compilationCaptor.getValue();

        assertThat(capturedCompilation.getId(), equalTo(compilation.getId()));
        assertThat(capturedCompilation.getTitle(), equalTo(compilation.getTitle()));
        assertThat(capturedCompilation.getEvents().size(), equalTo(2));
        assertThat(capturedCompilation.getPinned(), equalTo(compilation.getPinned()));

        assertThat(result.getId(), equalTo(compilation.getId()));
        assertThat(result.getTitle(), equalTo(compilation.getTitle()));
        assertThat(result.getEvents().size(), equalTo(2));
        assertThat(result.getEvents().stream()
                .map(EventShortDto::getId)
                .toList(), containsInAnyOrder(event1.getId(), event2.getId()));

        assertThat(result.getEvents().stream()
                .map(EventShortDto::getTitle)
                .toList(), containsInAnyOrder(event1.getTitle(), event2.getTitle()));

    }


    @Test
    void shouldUpdateOnlyProvidedFieldsWithoutEvents() {

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

        NewCompilationDto compilationDto = CompilationTestData
                .makeNewCompilationDto(null)
                .pinned(null)
                .title("Updated title")
                .build();

        Compilation compilation = CompilationTestData.makeNewCompilation(Set.of(event1, event2))
                .title("Updated title")
                .build();

        Mockito.when(compilationRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(compilation));

        Mockito.when(eventRepository.findAllByIdAsSet(Mockito.isNull()))
                .thenReturn(Set.of());

        Mockito.when(compilationRepository.save(Mockito.any(Compilation.class)))
                .thenAnswer(invocationOnMock -> {
                    Compilation returnCompilation = invocationOnMock.getArgument(0);
                    returnCompilation.setId(compilation.getId());
                    return returnCompilation;
                });

        Mockito.when(clientHandler.getStatsForEvents(Mockito.anyCollection(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of());

        Mockito.when(requestRepository.findAllByStatusAndEventIn(Mockito.eq(RequestStatus.CONFIRMED),
                        Mockito.anyCollection()))
                .thenReturn(List.of());

        CompilationDto result = compilationAdminService.update(1L, compilationDto);

        Mockito.verify(eventRepository).findAllByIdAsSet(Mockito.eq(compilationDto.getEvents()));

        ArgumentCaptor<Compilation> compilationCaptor = ArgumentCaptor.forClass(Compilation.class);
        Mockito.verify(compilationRepository).save(compilationCaptor.capture());
        Compilation capturedCompilation = compilationCaptor.getValue();

        assertThat(capturedCompilation.getId(), equalTo(compilation.getId()));
        assertThat(capturedCompilation.getTitle(), equalTo(compilation.getTitle()));
        assertThat(capturedCompilation.getEvents().size(), equalTo(2));
        assertThat(capturedCompilation.getPinned(), equalTo(compilation.getPinned()));

        assertThat(result.getId(), equalTo(compilation.getId()));
        assertThat(result.getTitle(), equalTo(compilation.getTitle()));
        assertThat(result.getEvents().size(), equalTo(2));
        assertThat(result.getEvents().stream()
                .map(EventShortDto::getId)
                .toList(), containsInAnyOrder(event1.getId(), event2.getId()));

        assertThat(result.getEvents().stream()
                .map(EventShortDto::getTitle)
                .toList(), containsInAnyOrder(event1.getTitle(), event2.getTitle()));

    }

    @Test
    void shouldThrowWhenCompilationNotFound() {

        Mockito.when(compilationRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> compilationAdminService.update(1L, new NewCompilationDto()));

    }


    @Test
    void shouldDeleteCompilation() {
        Compilation compilation = CompilationTestData.makeNewCompilation(Set.of())
                .build();

        Mockito.when(compilationRepository.findById(compilation.getId()))
                .thenReturn(Optional.of(compilation));

        compilationAdminService.delete(compilation.getId());

        Mockito.verify(compilationRepository).delete(compilation);
        Mockito.verify(eventRepository, Mockito.never()).findAllByIdAsSet(Mockito.anyCollection());
        Mockito.verify(clientHandler, Mockito.never()).getStatsForEvents(Mockito.anyCollection(),
                Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class));
        Mockito.verify(requestRepository, Mockito.never())
                .findAllByStatusAndEventIn(Mockito.any(), Mockito.anyCollection());
    }

    @Test
    void shouldThrowWhenDeleteCompilationNotFound() {
        long compilationId = 1L;

        Mockito.when(compilationRepository.findById(compilationId))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> compilationAdminService.delete(compilationId));

        assertThat(exception.getMessage(), containsString("Compilation with id=%s was not found"));

        Mockito.verify(compilationRepository, Mockito.never()).delete(Mockito.any());
        Mockito.verify(eventRepository, Mockito.never()).findAllByIdAsSet(Mockito.anyCollection());
        Mockito.verify(clientHandler, Mockito.never()).getStatsForEvents(Mockito.anyCollection(),
                Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class));
        Mockito.verify(requestRepository, Mockito.never())
                .findAllByStatusAndEventIn(Mockito.any(), Mockito.anyCollection());
    }
}
