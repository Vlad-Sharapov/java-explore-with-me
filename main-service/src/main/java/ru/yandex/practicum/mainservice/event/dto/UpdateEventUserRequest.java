package ru.yandex.practicum.mainservice.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.mainservice.event.enums.userenum.UsrStateAction;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest extends NewEventDto {

    private UsrStateAction stateAction;

}
