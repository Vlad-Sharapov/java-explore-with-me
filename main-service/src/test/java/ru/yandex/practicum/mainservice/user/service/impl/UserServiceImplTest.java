package ru.yandex.practicum.mainservice.user.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.user.dto.UserDto;
import ru.yandex.practicum.mainservice.user.model.User;
import ru.yandex.practicum.mainservice.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldAddUser() {
        UserDto userDto = UserDto.builder()
                .name("Ben")
                .email("ben@mail.ru")
                .build();

        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        UserDto result = userService.add(userDto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(captor.capture());

        User capturedUser = captor.getValue();
        assertThat(capturedUser.getName(), equalTo(userDto.getName()));
        assertThat(capturedUser.getEmail(), equalTo(userDto.getEmail()));

        assertThat(result.getId(), equalTo(1L));
        assertThat(result.getName(), equalTo(userDto.getName()));
        assertThat(result.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void shouldGetAllUsersWithoutIdsFilter() {
        User user1 = User.builder()
                .id(1L)
                .name("Ben")
                .email("ben@mail.ru")
                .build();
        User user2 = User.builder()
                .id(2L)
                .name("Ann")
                .email("ann@mail.ru")
                .build();

        Mockito.when(userRepository.findAll(Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(user1, user2)));

        List<UserDto> result = userService.getAll(null, 20, 10);

        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        Mockito.verify(userRepository).findAll(captor.capture());
        Mockito.verify(userRepository, Mockito.never())
                .findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class));

        PageRequest pageRequest = captor.getValue();
        assertThat(pageRequest.getPageNumber(), equalTo(2));
        assertThat(pageRequest.getPageSize(), equalTo(10));

        assertThat(result, hasSize(2));
        assertThat(result.stream()
                .map(UserDto::getId)
                .toList(), contains(user1.getId(), user2.getId()));
        assertThat(result.stream()
                .map(UserDto::getEmail)
                .toList(), contains(user1.getEmail(), user2.getEmail()));
    }

    @Test
    void shouldGetAllUsersByIds() {
        User user1 = User.builder()
                .id(1L)
                .name("Ben")
                .email("ben@mail.ru")
                .build();
        User user2 = User.builder()
                .id(2L)
                .name("Ann")
                .email("ann@mail.ru")
                .build();

        Mockito.when(userRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(user1, user2)));

        List<UserDto> result = userService.getAll(List.of(1L, 2L), 0, 10);

        Mockito.verify(userRepository).findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class));
        Mockito.verify(userRepository, Mockito.never()).findAll(Mockito.any(PageRequest.class));

        assertThat(result, hasSize(2));
        assertThat(result.stream()
                .map(UserDto::getId)
                .toList(), contains(user1.getId(), user2.getId()));
    }

    @Test
    void shouldDeleteUser() {
        User user = User.builder()
                .id(1L)
                .name("Ben")
                .email("ben@mail.ru")
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        userService.delete(user.getId());

        Mockito.verify(userRepository).findById(user.getId());
        Mockito.verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowExceptionWhenDeleteUserDoesNotExist() {
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.delete(111L));

        Mockito.verify(userRepository).findById(111L);
        Mockito.verify(userRepository, Mockito.never()).delete(Mockito.any(User.class));
    }
}
