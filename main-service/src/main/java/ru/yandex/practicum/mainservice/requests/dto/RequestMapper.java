package ru.yandex.practicum.mainservice.requests.dto;

import ru.yandex.practicum.mainservice.requests.model.Request;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RequestMapper {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public static ParticipationRequestDto toParticipationRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated().format(formatter))
                .requester(request.getRequester().getId())
                .event(request.getEvent().getId())
                .status(request.getStatus())
                .build();
    }

    public static List<ParticipationRequestDto> toParticipationRequestDto(Collection<Request> request) {
        return request.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

}
