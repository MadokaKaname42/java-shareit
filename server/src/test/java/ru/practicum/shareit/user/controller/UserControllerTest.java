package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_shouldReturnUsers() throws Exception {
        when(userService.getAll()).thenReturn(
                List.of(
                        new UserDto(1L, "Vanya", "v@gmail.com"),
                        new UserDto(2L, "Asuka", "v@gmail.com")
                )
        );

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].name").value("Asuka"));

        verify(userService).getAll();
    }

    @Test
    void getById_shouldReturnUser() throws Exception {
        UserDto user = new UserDto(1L, "madoka", "madoka@gmail.com");

        when(userService.getById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("madoka"))
                .andExpect(jsonPath("$.email").value("madoka@gmail.com"));

        verify(userService).getById(1L);
    }

    @Test
    void create_shouldReturnCreatedUser() throws Exception {
        UserDto input = new UserDto(null, "Madoka", "Madoka@mail.com");
        UserDto saved = new UserDto(1L, "Madoka", "Madoka@mail.com");

        when(userService.create(any(UserDto.class))).thenReturn(saved);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Madoka"));

        verify(userService).create(any(UserDto.class));
    }

    @Test
    void update_shouldReturnUpdatedUser() throws Exception {
        UserDto update = new UserDto(null, "NewName", "new@mail.com");
        UserDto updated = new UserDto(1L, "NewName", "new@mail.com");

        when(userService.update(any(UserDto.class), eq(1L)))
                .thenReturn(updated);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"))
                .andExpect(jsonPath("$.email").value("new@mail.com"));

        verify(userService).update(any(UserDto.class), eq(1L));
    }

    @Test
    void delete_shouldReturnOk() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isOk());

        verify(userService).delete(1L);
    }

    @Test
    void getById_whenUserNotFound_shouldReturn404() throws Exception {
        when(userService.getById(99L))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(userService).getById(99L);
    }

    @Test
    void update_whenUserNotFound_shouldReturn404() throws Exception {
        UserDto update = new UserDto(null, "Alex", "alex@mail.com");

        when(userService.update(any(UserDto.class), eq(99L)))
                .thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(patch("/users/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());

        verify(userService).update(any(UserDto.class), eq(99L));
    }

    @Test
    void delete_whenUserNotFound_shouldReturn404() throws Exception {
        doThrow(new NotFoundException("User not found"))
                .when(userService).delete(99L);

        mockMvc.perform(delete("/users/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(userService).delete(99L);
    }
}



