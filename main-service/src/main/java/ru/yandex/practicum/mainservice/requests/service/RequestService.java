package ru.yandex.practicum.mainservice.requests.service;

import ru.yandex.practicum.mainservice.requests.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto addRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto canceledRequest(Long userId, Long requestId);

}
