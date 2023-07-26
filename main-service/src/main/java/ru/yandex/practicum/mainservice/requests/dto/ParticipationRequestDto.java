package ru.yandex.practicum.mainservice.requests.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.mainservice.requests.enums.RequestStatus;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {

    private long id;

    private String created;

    private long event;

    private long requester;

    private RequestStatus status;

}
