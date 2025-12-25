package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public List<UserDto> getAll() {
        return userRepository.findAll().stream().map(UserMapper::toUserDto).collect(toList());
    }

    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден ID: " + id));

        return UserMapper.toUserDto(user);
    }

    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        throwIfEmailNotUnique(user);

        return UserMapper.toUserDto(userRepository.create(user));
    }

    public UserDto update(UserDto userDto, Long id) {
        User user = UserMapper.toUser(userDto);
        User updatedUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден id = {}" + id));
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            throwIfEmailNotUnique(user);
            updatedUser.setEmail(user.getEmail());
        }
        if (user.getName() != null && !user.getName().isBlank()) {
            updatedUser.setName(user.getName());
        }

        return UserMapper.toUserDto(updatedUser);
    }

    public void delete(Long id) {
        getById(id);
        userRepository.delete(id);
    }

    private void throwIfEmailNotUnique(User user) {
        for (User userCheck : userRepository.findAll()) {
            if (user.getEmail().equals(userCheck.getEmail()) && !Objects.equals(user.getId(), userCheck.getId())) {
                throw new ValidationException("Пользователь с email + " + user.getEmail() + " уже зарегистрирован");
            }
        }
    }
}
