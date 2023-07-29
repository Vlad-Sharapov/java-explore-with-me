package ru.yandex.practicum.mainservice.compilation.service.admin;

import ru.yandex.practicum.mainservice.compilation.dto.CompilationDto;
import ru.yandex.practicum.mainservice.compilation.dto.NewCompilationDto;

public interface CompilationAdminService {

    CompilationDto add(NewCompilationDto compilationDto);

    CompilationDto update(long compilationId, NewCompilationDto compilationDto);

    void delete(long compilationId);

}
