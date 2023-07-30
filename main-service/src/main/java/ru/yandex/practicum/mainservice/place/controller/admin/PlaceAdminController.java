package ru.yandex.practicum.mainservice.place.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.mainservice.place.dto.PlaceDto;
import ru.yandex.practicum.mainservice.place.service.admin.LocationAdminService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/locations")
public class PlaceAdminController {

    private final LocationAdminService locationAdminService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceDto add(@Valid @RequestBody PlaceDto locationDtoForAdmin) {
        return locationAdminService.add(locationDtoForAdmin);
    }

    @PatchMapping
    public PlaceDto update(@Valid @RequestBody PlaceDto locationDtoForAdmin) {
        return locationAdminService.update(locationDtoForAdmin);
    }

    @DeleteMapping("/{locationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long locationId) {
        locationAdminService.delete(locationId);
    }

    @GetMapping
    public List<PlaceDto> locations(@RequestParam(required = false) String text,
                                    @RequestParam(defaultValue = "0") int from,
                                    @RequestParam(defaultValue = "10") int size) {
        return locationAdminService.getPlaces(text, from, size);
    }
}
