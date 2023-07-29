package ru.yandex.practicum.mainservice.location.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetLocationsForAdminRequest {

    private String text;

    private Boolean untitled;

    private Integer from;

    private Integer size;

    public static GetLocationsForAdminRequest of(String text,
                                                 Boolean untitled,
                                                 Integer from, Integer size) {

        return GetLocationsForAdminRequest.builder()
                .text(text)
                .untitled(untitled)
                .from(from)
                .size(size)
                .build();
    }
}
