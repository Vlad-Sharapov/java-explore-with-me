package ru.yandex.practicum.mainservice.compilation.service.publics.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mainservice.compilation.dto.CompilationDto;
import ru.yandex.practicum.mainservice.compilation.model.Compilation;
import ru.yandex.practicum.mainservice.compilation.model.QCompilation;
import ru.yandex.practicum.mainservice.compilation.repository.CompilationRepository;
import ru.yandex.practicum.mainservice.compilation.service.publics.CompilationPublicService;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.stateclient.ClientHandler;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.requests.model.Request;
import ru.yandex.practicum.mainservice.requests.repository.RequestRepository;
import ru.yandex.practicum.statdto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.mainservice.compilation.dto.CompilationMapper.toCompilationDto;
import static ru.yandex.practicum.mainservice.requests.enums.RequestStatus.CONFIRMED;


@Service
@RequiredArgsConstructor
public class CompilationPublicServiceImpl implements CompilationPublicService {

    private final RequestRepository requestRepository;

    private final ClientHandler clientHandler;

    private final CompilationRepository compilationRepository;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        PageRequest pageRequest = PageRequest
                .of(from > 0 ? from / size : 0, size);
        QCompilation event = QCompilation.compilation;
        List<Compilation> compilations;

        if (pinned != null) {
            compilations = compilationRepository.findAll(event.pinned.eq(pinned), pageRequest).getContent();
        } else {
            compilations = compilationRepository.findAll(pageRequest).getContent();
        }

        List<Event> compilationsEvents = compilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .collect(Collectors.toList());

        List<StatsDto> statsDtos = clientHandler
                .getStatsForEvents(compilationsEvents, LocalDateTime.now().minusYears(100), LocalDateTime.now());

        List<Request> confirmedRequests = requestRepository.findAllByStatusAndEventIn(CONFIRMED, compilationsEvents);

        return toCompilationDto(compilations, statsDtos, confirmedRequests);
    }

    @Override
    public CompilationDto getCompilation(long compilationId) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new EntityNotFoundException("Compilation with id=%s was not found"));

        List<StatsDto> statsDtos = clientHandler
                .getStatsForEvents(compilation.getEvents(), LocalDateTime.now().minusYears(100), LocalDateTime.now());

        List<Request> confirmedRequests = requestRepository.findAllByStatusAndEventIn(CONFIRMED, compilation.getEvents());

        return toCompilationDto(compilation, statsDtos, confirmedRequests);
    }
}
