package ru.yandex.practicum.mainservice.requests.model;

import lombok.Data;
import ru.yandex.practicum.mainservice.requests.enums.RequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {

    private List<Long> requestIds;

    private RequestStatus status;

}
