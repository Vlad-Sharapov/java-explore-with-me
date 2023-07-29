package ru.yandex.practicum.mainservice.compilation.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mainservice.compilation.dto.CompilationDto;
import ru.yandex.practicum.mainservice.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.mainservice.compilation.model.Compilation;
import ru.yandex.practicum.mainservice.compilation.repository.CompilationRepository;
import ru.yandex.practicum.mainservice.compilation.service.admin.CompilationAdminService;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.repository.EventRepository;
import ru.yandex.practicum.mainservice.event.stateclient.ClientHandler;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.requests.model.Request;
import ru.yandex.practicum.mainservice.requests.repository.RequestRepository;
import ru.yandex.practicum.statdto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static ru.yandex.practicum.mainservice.compilation.dto.CompilationMapper.toCompilation;
import static ru.yandex.practicum.mainservice.compilation.dto.CompilationMapper.toCompilationDto;
import static ru.yandex.practicum.mainservice.requests.enums.RequestStatus.CONFIRMED;


@Service
@RequiredArgsConstructor
public class CompilationAdminServiceImpl implements CompilationAdminService {

    private final EventRepository eventRepository;

    private final RequestRepository requestRepository;

    private final ClientHandler clientHandler;

    private final CompilationRepository compilationRepository;

    @Override
    public CompilationDto add(NewCompilationDto compilationDto) {
        Set<Event> events = eventRepository.findAllByIdAsSet(compilationDto.getEvents());
        Compilation savedCompilation = compilationRepository.save(toCompilation(compilationDto, events));
        List<StatsDto> statsDtos = clientHandler.getStatsForEvents(events, LocalDateTime.now().minusYears(100), LocalDateTime.now());
        List<Request> confirmedRequests = requestRepository.findAllByStatusAndEventIn(CONFIRMED, events);
        return toCompilationDto(savedCompilation, statsDtos, confirmedRequests);
    }

    @Override
    public CompilationDto update(long compilationId, NewCompilationDto compilationDto) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Compilation with id=%s was not found", compilationId)));
        Set<Event> events = eventRepository.findAllByIdAsSet(compilationDto.getEvents());
        Compilation updatedCompilation = updateCompilation(compilationDto, compilation, events);
        Compilation savedCompilation = compilationRepository.save(updatedCompilation);
        List<StatsDto> statsDtos = clientHandler.getStatsForEvents(events, LocalDateTime.now().minusYears(100), LocalDateTime.now());
        List<Request> confirmedRequests = requestRepository.findAllByStatusAndEventIn(CONFIRMED, events);
        return toCompilationDto(savedCompilation, statsDtos, confirmedRequests);
    }

    @Override
    public void delete(long compilationId) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new EntityNotFoundException("Compilation with id=%s was not found"));
        compilationRepository.delete(compilation);
    }

    private Compilation updateCompilation(NewCompilationDto newCompilationDto,
                                          Compilation compilation,
                                          Set<Event> events) {
        Compilation.CompilationBuilder builder = compilation.toBuilder();
        if (newCompilationDto.getEvents() != null) {
            builder.events(events);
        }
        if (newCompilationDto.getPinned() != null) {
            builder.pinned(newCompilationDto.getPinned());
        }
        if (newCompilationDto.getTitle() != null) {
            builder.title(newCompilationDto.getTitle());
        }
        return builder.build();
    }
}
