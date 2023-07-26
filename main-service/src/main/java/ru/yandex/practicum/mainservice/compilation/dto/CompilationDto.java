package ru.yandex.practicum.mainservice.compilation.dto;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.mainservice.event.dto.EventShortDto;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class CompilationDto {

    private long id;

    private Set<EventShortDto> events;

    private Boolean pinned;

    @NotBlank(message = "Field: title. Error: must not be blank.")
    private String title;
}
