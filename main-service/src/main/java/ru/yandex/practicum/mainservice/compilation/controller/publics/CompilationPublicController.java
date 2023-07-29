package ru.yandex.practicum.mainservice.compilation.controller.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.mainservice.compilation.dto.CompilationDto;
import ru.yandex.practicum.mainservice.compilation.service.publics.CompilationPublicService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class CompilationPublicController {

    private final CompilationPublicService compilationPublicService;

    @GetMapping
    public List<CompilationDto> compilations(@RequestParam(required = false) Boolean pinned,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        return compilationPublicService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compilationId}")
    public CompilationDto compilation(@PathVariable long compilationId) {
        return compilationPublicService.getCompilation(compilationId);
    }
}
