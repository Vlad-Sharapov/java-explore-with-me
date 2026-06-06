package ru.yandex.practicum.mainservice.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.mainservice.compilation.utils.Marker;

import java.util.Set;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {

    private long id;

    private Set<Long> events;

    private Boolean pinned = false;

    @NotBlank(groups = Marker.OnCreate.class,
            message = "Field: title. Error: Must not be less than 1 character and more than 50 characters.")
    @Size(groups = {Marker.OnCreate.class, Marker.OnUpdate.class}, min = 1, max = 50,
            message = "Field: title. Error: Must not be less than 1 character and more than 50 characters.")
    private String title;

}
