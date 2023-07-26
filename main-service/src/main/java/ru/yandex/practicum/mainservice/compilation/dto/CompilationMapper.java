package ru.yandex.practicum.mainservice.compilation.dto;

import ru.yandex.practicum.mainservice.compilation.model.Compilation;
import ru.yandex.practicum.mainservice.event.dto.EventShortDto;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.requests.model.Request;
import ru.yandex.practicum.statdto.StatsDto;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.yandex.practicum.mainservice.event.dto.EventMapper.toEventShortDto;


public class CompilationMapper {

    public static Compilation toCompilation(NewCompilationDto compilationDto, Set<Event> events) {
        return Compilation.builder()
                .id(compilationDto.getId())
                .pinned(compilationDto.getPinned())
                .events(events)
                .title(compilationDto.getTitle())
                .build();
    }

    public static CompilationDto toCompilationDto(Compilation compilation,
                                                  Collection<StatsDto> statsDtos,
                                                  List<Request> confirmedRequests) {
        List<EventShortDto> eventShortDtos = toEventShortDto(compilation.getEvents(), statsDtos, confirmedRequests);

        return CompilationDto.builder()
                .id(compilation.getId())
                .events(Set.copyOf(eventShortDtos))
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public static List<CompilationDto> toCompilationDto(Collection<Compilation> compilations,
                                                        Collection<StatsDto> statsDtos,
                                                        List<Request> confirmedRequests) {
        return compilations.stream()
                .map(compilation -> toCompilationDto(compilation, statsDtos, confirmedRequests))
                .collect(Collectors.toList());
    }
}
