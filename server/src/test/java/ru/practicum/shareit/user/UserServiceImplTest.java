package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Alice", "alice@mail.com");
        userDto = new UserDto(1L, "Alice", "alice@mail.com");
    }

    @Test
    void createWhenEmailDuplicateThenThrowValidationException() {
        User existingUser = new User(1L, "Alice", "alice@mail.com");
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        UserDto newUser = new UserDto(null, "Bob", "alice@mail.com");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.create(newUser)
        );

        assertThat(exception.getMessage()).contains("alice@mail.com");
        verify(userRepository, never()).save(any());
    }


    @Test
    void createWhenValidThenSaveAndReturnDto() {
        UserDto newUser = new UserDto(null, "Alice", "alice@mail.com");
        User model = new User(null, "Alice", "alice@mail.com");
        when(userMapper.userDtoToUserModel(newUser)).thenReturn(model);
        when(userRepository.findAll()).thenReturn(List.of());
        when(userRepository.save(model)).thenReturn(user);
        when(userMapper.userModelToUserDto(user)).thenReturn(userDto);

        UserDto result = userService.create(newUser);

        assertThat(result).isEqualTo(userDto);
        verify(userRepository).save(model);
    }

    // -------------------------------------------
    // update()
    // -------------------------------------------
    @Test
    void updateWhenUserNotFoundThenThrowNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        UserDto dto = new UserDto(99L, "Bob", "bob@mail.com");

        assertThrows(NotFoundException.class, () -> userService.update(dto, 99L));
    }

    @Test
    void updateWhenEmailDuplicateThenThrowValidationException() {
        UserDto dto = new UserDto(1L, "Alice", "duplicate@mail.com");

        User existing = new User(1L, "Alice", "alice@mail.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        User another = new User(2L, "Bob", "duplicate@mail.com");
        when(userRepository.findAll()).thenReturn(List.of(existing, another));

        lenient().when(userMapper.userDtoToUserModel(dto))
                .thenReturn(new User(1L, "Alice", "duplicate@mail.com"));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.update(dto, 1L)
        );

        assertThat(exception.getMessage()).contains("duplicate@mail.com");
        verify(userRepository, never()).save(any());
    }


    @Test
    void updateWhenValidThenReturnUpdated() {
        UserDto dto = new UserDto(1L, "Bob", "bob@mail.com");
        User existing = new User(1L, "Alice", "alice@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findAll()).thenReturn(List.of(existing)); // все элементы не null
        when(userMapper.userModelToUserDto(existing)).thenReturn(dto);

        UserDto result = userService.update(dto, 1L);

        assertThat(result).isEqualTo(dto);
        assertThat(existing.getName()).isEqualTo("Bob");
        assertThat(existing.getEmail()).isEqualTo("bob@mail.com");

        verify(userRepository).save(existing);
    }


    @Test
    void getByIdWhenExistsThenReturnDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.userModelToUserDto(user)).thenReturn(userDto);

        UserDto result = userService.getById(1L);

        assertThat(result).isEqualTo(userDto);
    }

    @Test
    void getByIdWhenNotExistsThenThrowNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getById(1L));
    }

    // -------------------------------------------
    // getAll()
    // -------------------------------------------
    @Test
    void getAllWhenUsersExistThenReturnList() {
        User user2 = new User(2L, "Bob", "bob@mail.com");
        UserDto dto2 = new UserDto(2L, "Bob", "bob@mail.com");
        when(userRepository.findAll()).thenReturn(List.of(user, user2));
        when(userMapper.userModelToUserDto(user)).thenReturn(userDto);
        when(userMapper.userModelToUserDto(user2)).thenReturn(dto2);

        List<UserDto> result = userService.getAll();

        assertThat(result).containsExactlyInAnyOrder(userDto, dto2);
    }

    @Test
    void getAllWhenEmptyThenReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> result = userService.getAll();

        assertThat(result).isEmpty();
    }

    // -------------------------------------------
    // delete()
    // -------------------------------------------
    @Test
    void deleteWhenExistsThenDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteWhenNotFoundThenThrowNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.delete(1L));
        verify(userRepository, never()).delete(any());
    }
}
