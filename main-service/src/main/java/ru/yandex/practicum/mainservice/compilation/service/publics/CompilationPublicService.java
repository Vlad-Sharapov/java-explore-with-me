package ru.yandex.practicum.mainservice.compilation.service.publics;

import ru.yandex.practicum.mainservice.compilation.dto.CompilationDto;

import java.util.List;

public interface CompilationPublicService {

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilation(long compilationId);

}
