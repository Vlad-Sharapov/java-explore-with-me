package ru.yandex.practicum.mainservice.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private long id;

    @NotBlank(message = "Field: name. Error: must not be blank.")
    @Size(min = 1, max = 50,
            message = "Field: name. Error: Must not be less than 1 characters and more than 50 characters.")
    private String name;


}
