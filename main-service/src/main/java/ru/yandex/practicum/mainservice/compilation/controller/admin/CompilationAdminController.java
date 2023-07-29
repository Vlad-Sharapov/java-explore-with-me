package ru.yandex.practicum.mainservice.compilation.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.mainservice.compilation.dto.CompilationDto;
import ru.yandex.practicum.mainservice.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.mainservice.compilation.service.admin.CompilationAdminService;
import ru.yandex.practicum.mainservice.compilation.utils.Marker;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class CompilationAdminController {

    private final CompilationAdminService compilationAdminService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto add(@Validated({Marker.OnCreate.class}) @RequestBody NewCompilationDto compilationDto) {
        return compilationAdminService.add(compilationDto);
    }

    @PatchMapping("/{compilationId}")
    public CompilationDto update(@PathVariable long compilationId,
                                 @Validated({Marker.OnUpdate.class}) @RequestBody NewCompilationDto compilationDto) {
        return compilationAdminService.update(compilationId, compilationDto);
    }

    @DeleteMapping("/{compilationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long compilationId) {
        compilationAdminService.delete(compilationId);
    }
}
