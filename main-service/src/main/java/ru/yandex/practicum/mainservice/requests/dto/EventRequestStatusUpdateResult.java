package ru.yandex.practicum.mainservice.requests.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class EventRequestStatusUpdateResult {

    private List<ParticipationRequestDto> confirmedRequests;

    private List<ParticipationRequestDto> rejectedRequests;

}
