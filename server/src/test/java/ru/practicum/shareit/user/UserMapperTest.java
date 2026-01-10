package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    void userDtoToUserModelShouldMapFields() {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("Alice");
        dto.setEmail("alice@mail.com");

        User user = mapper.userDtoToUserModel(dto);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("Alice");
        assertThat(user.getEmail()).isEqualTo("alice@mail.com");
    }

    @Test
    void userModelToUserDtoShouldMapFields() {
        User user = new User(2L, "Bob", "bob@mail.com");

        UserDto dto = mapper.userModelToUserDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getName()).isEqualTo("Bob");
        assertThat(dto.getEmail()).isEqualTo("bob@mail.com");
    }
}

