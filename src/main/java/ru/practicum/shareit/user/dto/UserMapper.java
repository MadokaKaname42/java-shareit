package ru.practicum.shareit.user.dto;

import org.mapstruct.Mapper;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User userDtoToUserModel(UserDto userDto);

    UserDto userModelToUserDto(User user);

    UserShortDto userModelToUserShortDto(User user);
}
