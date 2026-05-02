package ru.yandex.practicum.mainservice.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    @NotBlank(message = "Field: name. Error: must not be blank.")
    @Size(min = 2, max = 250,
            message = "Field: name. Error: Must not be less than 2 characters and more than 250 characters.")
    private String name;

    @Email(message = "Incorrect email has been entered")
    @NotBlank(message = "Field: email. Error: must not be blank. Value: null")
    @Size(min = 6, max = 254,
            message = "Field: email. Error: Must not be less than 6 characters and more than 254 characters.")
    private String email;
}
