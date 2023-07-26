package ru.yandex.practicum.statdto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class StatParamDto {

    private LocalDateTime start;
    private LocalDateTime end;
    private List<String> uris;
    private Boolean unique;


    public boolean hasUris() {
        return uris != null && !uris.isEmpty();
    }

    public boolean hasUnique() {
        return unique != null;
    }


}
