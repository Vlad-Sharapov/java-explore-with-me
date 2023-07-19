package ru.yandex.practicum.statservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Stats {

    private String app;
    private String uri;
    private Long hits;

}
