package ru.yandex.practicum.mainservice.user.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.user.dto.UserDto;
import ru.yandex.practicum.mainservice.user.dto.UserMapper;
import ru.yandex.practicum.mainservice.user.model.QUser;
import ru.yandex.practicum.mainservice.user.model.User;
import ru.yandex.practicum.mainservice.user.repository.UserRepository;
import ru.yandex.practicum.mainservice.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.mainservice.user.dto.UserMapper.toUser;
import static ru.yandex.practicum.mainservice.user.dto.UserMapper.toUserDto;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto add(UserDto userDto) {
        UserDto response = toUserDto(userRepository.save(toUser(userDto)));
        log.info("A new user has registered: {}", response);
        return response;
    }

    @Override
    public List<UserDto> getAll(List<Long> ids, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        QUser qUser = QUser.user;
        Page<User> allUsers;
        if (ids != null) {
            BooleanExpression userCondition = qUser.id.in(ids);
            allUsers = userRepository.findAll(userCondition, pageRequest);
        } else {
            allUsers = userRepository.findAll(pageRequest);
        }
        log.info("A list of all users has been received.");
        return allUsers.stream()
                .map((UserMapper::toUserDto))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void delete(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException(String.format("User with id=%s was not found", userId)));
        userRepository.delete(user);
        log.info("User {} has been deleted.", user);
    }

}
