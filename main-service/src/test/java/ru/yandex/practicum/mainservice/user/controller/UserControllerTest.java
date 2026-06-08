package ru.yandex.practicum.mainservice.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mainservice.TestUtils;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.user.dto.UserDto;
import ru.yandex.practicum.mainservice.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldAddUser() throws Exception {
        UserDto newUserDto = UserDto.builder()
                .name("Ben")
                .email("ben@mail.ru")
                .build();

        UserDto userDto = newUserDto.toBuilder()
                .id(1L)
                .build();

        Mockito.when(userService.add(Mockito.any(UserDto.class)))
                .thenReturn(userDto);

        mockMvc.perform(post("/admin/users")
                        .content(TestUtils.asJsonString(objectMapper, newUserDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        Mockito.verify(userService).add(Mockito.any(UserDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        UserDto userDto = UserDto.builder()
                .name(null)
                .email("ben@mail.ru")
                .build();

        mockMvc.perform(post("/admin/users")
                        .content(TestUtils.asJsonString(objectMapper, userDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.message").value("Field: name. Error: must not be blank."))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(userService, Mockito.never()).add(Mockito.any(UserDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("Ben")
                .email("incorrect-email")
                .build();

        mockMvc.perform(post("/admin/users")
                        .content(TestUtils.asJsonString(objectMapper, userDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Incorrectly made request."))
                .andExpect(jsonPath("$.message").value("Incorrect email has been entered"))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(userService, Mockito.never()).add(Mockito.any(UserDto.class));
    }

    @Test
    void shouldGetAllUsersByIds() throws Exception {
        UserDto user1 = UserDto.builder()
                .id(1L)
                .name("Ben")
                .email("ben@mail.ru")
                .build();
        UserDto user2 = UserDto.builder()
                .id(2L)
                .name("Ann")
                .email("ann@mail.ru")
                .build();

        Mockito.when(userService.getAll(Mockito.anyList(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/admin/users")
                        .param("ids", "1", "2")
                        .param("from", "20")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(user1.getId()))
                .andExpect(jsonPath("$[0].name").value(user1.getName()))
                .andExpect(jsonPath("$[0].email").value(user1.getEmail()))
                .andExpect(jsonPath("$[1].id").value(user2.getId()))
                .andExpect(jsonPath("$[1].name").value(user2.getName()))
                .andExpect(jsonPath("$[1].email").value(user2.getEmail()));

        Mockito.verify(userService).getAll(List.of(1L, 2L), 20, 10);
    }

    @Test
    void shouldGetAllUsersWithDefaultParams() throws Exception {
        Mockito.when(userService.getAll(Mockito.isNull(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        Mockito.verify(userService).getAll(null, 0, 10);
    }

    @Test
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/admin/users/{userId}", 1L))
                .andExpect(status().isNoContent());

        Mockito.verify(userService).delete(1L);
    }

    @Test
    void shouldThrowNotFoundWhenDeleteUserDoesNotExist() throws Exception {
        Mockito.doThrow(new EntityNotFoundException("User with id=1 was not found"))
                .when(userService)
                .delete(Mockito.anyLong());

        mockMvc.perform(delete("/admin/users/{userId}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("404 NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("The required object was not found."))
                .andExpect(jsonPath("$.message").value(containsString("User with id=1")))
                .andExpect(jsonPath("$.timestamp").exists());

        Mockito.verify(userService).delete(1L);
    }
}
