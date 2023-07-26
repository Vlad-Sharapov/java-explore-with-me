package ru.yandex.practicum.mainservice.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


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
