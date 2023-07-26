package ru.yandex.practicum.mainservice.event.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import ru.yandex.practicum.mainservice.category.dto.CategoryDto;
import ru.yandex.practicum.mainservice.user.dto.UserDto;


@Data
@SuperBuilder(toBuilder = true)
public class EventShortDto {

    protected long id;

    protected UserDto initiator;

    protected String annotation;

    protected String title;

    protected long confirmedRequests;

    protected CategoryDto category;

    protected String description;

    protected String eventDate;

    protected boolean paid;

    protected long views;

}
