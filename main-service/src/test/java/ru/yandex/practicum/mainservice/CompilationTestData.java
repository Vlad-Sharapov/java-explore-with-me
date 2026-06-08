package ru.yandex.practicum.mainservice;

import ru.yandex.practicum.mainservice.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.mainservice.compilation.model.Compilation;
import ru.yandex.practicum.mainservice.event.model.Event;

import java.util.Set;

public final class CompilationTestData {

    private CompilationTestData() {
    }


    public static NewCompilationDto.NewCompilationDtoBuilder makeNewCompilationDto(Set<Long> events) {
        return NewCompilationDto.builder()
                .events(events)
                .pinned(false)
                .title("title");
    }

    public static Compilation.CompilationBuilder makeNewCompilation(Set<Event> events) {
        return Compilation.builder()
                .id(1L)
                .events(events)
                .pinned(false)
                .title("title");
    }


}
