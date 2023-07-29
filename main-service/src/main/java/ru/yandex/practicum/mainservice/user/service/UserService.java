package ru.yandex.practicum.mainservice.user.service;

import ru.yandex.practicum.mainservice.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto add(UserDto userDto);

    List<UserDto> getAll(List<Long> ids, int from, int size);

    void delete(Long userId);

}
