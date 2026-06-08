package ru.yandex.practicum.statdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StatsDto {

    private String app;
    private String uri;
    private Long hits;


}
