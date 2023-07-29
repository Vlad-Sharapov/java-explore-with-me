package ru.yandex.practicum.mainservice.location.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.mainservice.location.dto.LocationDtoForAdmin;
import ru.yandex.practicum.mainservice.location.model.GetLocationsForAdminRequest;
import ru.yandex.practicum.mainservice.location.service.admin.LocationAdminService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/locations")
public class LocationAdminController {

    private final LocationAdminService locationAdminService;

    @PostMapping
    public LocationDtoForAdmin add(@RequestBody LocationDtoForAdmin locationDtoForAdmin) {
        return locationAdminService.add(locationDtoForAdmin);
    }

    @PatchMapping
    public LocationDtoForAdmin update(@RequestBody LocationDtoForAdmin locationDtoForAdmin) {
        return locationAdminService.update(locationDtoForAdmin);
    }

    @DeleteMapping("/{locationId}")
    public void delete(@PathVariable Long locationId) {
        locationAdminService.delete(locationId);
    }

    @GetMapping
    public List<LocationDtoForAdmin> locations(@RequestParam(required = false) String text,
                                               @RequestParam(required = false) Boolean untitled,
                                               @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") int size) {
        return locationAdminService.getLocations(GetLocationsForAdminRequest.of(text, untitled, from, size));
    }
}
