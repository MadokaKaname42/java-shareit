package ru.practicum.shareit.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void createShouldSaveUser() {
        UserDto dto = new UserDto(null, "Test", "test@mail.ru");

        UserDto created = userService.create(dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Test");
        assertThat(created.getEmail()).isEqualTo("test@mail.ru");

        assertThat(userRepository.findById(created.getId())).isPresent();
    }

    @Test
    void getByIdShouldReturnUser() {
        UserDto created =
                userService.create(new UserDto(null, "Test", "test@mail.ru"));

        UserDto found = userService.getById(created.getId());

        assertThat(found.getEmail()).isEqualTo("test@mail.ru");
        assertThat(found.getName()).isEqualTo("Test");
    }

    @Test
    void getByIdShouldThrowIfNotFound() {
        assertThatThrownBy(() ->
                userService.getById(999L)
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllShouldReturnUsers() {
        userService.create(new UserDto(null, "U1", "u1@mail.ru"));
        userService.create(new UserDto(null, "U2", "u2@mail.ru"));

        List<UserDto> users = userService.getAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void updateShouldChangeNameAndEmail() {
        UserDto created =
                userService.create(new UserDto(null, "Old", "old@mail.ru"));

        UserDto update =
                new UserDto(null, "New", "new@mail.ru");

        UserDto updated =
                userService.update(update, created.getId());

        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getEmail()).isEqualTo("new@mail.ru");
    }

    @Test
    void updateShouldNotUpdateBlankName() {
        UserDto created =
                userService.create(new UserDto(null, "Name", "mail@mail.ru"));

        UserDto update =
                new UserDto(null, "   ", "new@mail.ru");

        UserDto updated =
                userService.update(update, created.getId());

        assertThat(updated.getName()).isEqualTo("Name");
        assertThat(updated.getEmail()).isEqualTo("new@mail.ru");
    }

    @Test
    void updateShouldThrowIfEmailUsedByAnotherUser() {
        UserDto u1 =
                userService.create(new UserDto(null, "U1", "u1@mail.ru"));
        UserDto u2 =
                userService.create(new UserDto(null, "U2", "u2@mail.ru"));

        assertThatThrownBy(() ->
                userService.update(
                        new UserDto(null, null, "u1@mail.ru"),
                        u2.getId()
                )
        ).isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void deleteShouldRemoveUser() {
        UserDto created =
                userService.create(new UserDto(null, "Test", "test@mail.ru"));

        userService.delete(created.getId());

        assertThat(userRepository.findById(created.getId())).isEmpty();
    }

    @Test
    void deleteShouldThrowIfNotFound() {
        assertThatThrownBy(() ->
                userService.delete(999L)
        ).isInstanceOf(NotFoundException.class);
    }
}

