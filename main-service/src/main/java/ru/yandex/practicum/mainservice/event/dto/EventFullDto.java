package ru.yandex.practicum.mainservice.event.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import ru.yandex.practicum.mainservice.event.enums.EventState;

@Getter
@SuperBuilder(toBuilder = true)
public class EventFullDto extends EventShortDto {

    private String createdOn;

    private LocationDto location;

    private int participantLimit;

    private String publishedOn;

    private boolean requestModeration;

    private EventState state;

}
