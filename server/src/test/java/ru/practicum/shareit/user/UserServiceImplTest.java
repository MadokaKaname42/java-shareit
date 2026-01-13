package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Alice", "alice@mail.com");
        userDto = new UserDto(1L, "Alice", "alice@mail.com");
    }

    @Test
    void getAll_shouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.userModelToUserDto(user)).thenReturn(userDto);

        List<UserDto> users = userService.getAll();

        assertThat(users).hasSize(1);
        assertThat(users.get(0)).isEqualTo(userDto);
    }

    @Test
    void getById_existingUser_shouldReturnDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.userModelToUserDto(user)).thenReturn(userDto);

        UserDto result = userService.getById(1L);

        assertThat(result).isEqualTo(userDto);
    }

    @Test
    void getById_nonExistingUser_shouldThrowNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getById(99L));
        assertTrue(ex.getMessage().contains("Пользователь не найден"));
    }

    @Test
    void createUser_shouldReturnDto() {
        when(userMapper.userDtoToUserModel(userDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.userModelToUserDto(user)).thenReturn(userDto);

        UserDto result = userService.create(userDto);

        assertThat(result).isEqualTo(userDto);
        verify(userRepository).save(user);
    }

    @Test
    void createUser_duplicateEmail_shouldThrow() {
        when(userMapper.userDtoToUserModel(userDto)).thenReturn(user);
        when(userRepository.save(user)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.create(userDto));
    }

    @Test
    void update_existingUser_shouldChangeNameAndEmail() {
        UserDto updateDto = new UserDto(null, "Bob", "bob@mail.com");
        User updatedUser = new User(1L, "Bob", "bob@mail.com");
        UserDto updatedDto = new UserDto(1L, "Bob", "bob@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("bob@mail.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.userModelToUserDto(updatedUser)).thenReturn(updatedDto);

        UserDto result = userService.update(updateDto, 1L);

        assertThat(result.getName()).isEqualTo("Bob");
        assertThat(result.getEmail()).isEqualTo("bob@mail.com");
    }

    @Test
    void update_nonExistingUser_shouldThrow() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserDto updateDto = new UserDto(null, "Bob", "bob@mail.com");

        assertThrows(NotFoundException.class, () -> userService.update(updateDto, 99L));
    }

    @Test
    void update_duplicateEmail_shouldThrow() {
        UserDto updateDto = new UserDto(null, null, "bob@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("bob@mail.com", 1L)).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.update(updateDto, 1L));
    }

    @Test
    void delete_existingUser_shouldCallRepositoryDelete() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void delete_nonExistingUser_shouldThrow() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.delete(99L));
    }
}

