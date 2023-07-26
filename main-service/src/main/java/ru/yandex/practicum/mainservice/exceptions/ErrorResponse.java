package ru.yandex.practicum.mainservice.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private String status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String reason;

    private String message;

    private String timestamp;

}
