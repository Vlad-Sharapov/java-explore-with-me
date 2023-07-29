package ru.yandex.practicum.mainservice.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.mainservice.event.enums.adminenum.AdmStateAction;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminRequest extends NewEventDto {

    private AdmStateAction stateAction;

}
