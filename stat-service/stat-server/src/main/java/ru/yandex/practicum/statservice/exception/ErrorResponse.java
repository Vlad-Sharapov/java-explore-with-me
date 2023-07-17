package ru.yandex.practicum.statservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;

}
