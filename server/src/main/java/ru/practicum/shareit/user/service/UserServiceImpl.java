package ru.practicum.shareit.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream().map(userMapper::userModelToUserDto).collect(toList());
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден id: " + id));

        return userMapper.userModelToUserDto(user);
    }

    @Transactional
    @Override
    public UserDto create(UserDto userDto) {
        throwIfEmailNotUnique(userDto);
        User user = userMapper.userDtoToUserModel(userDto);
        return userMapper.userModelToUserDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserDto update(UserDto userDto, Long id) {
        User updatedUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден id: " + id));

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            throwIfEmailNotUnique(userDto);
            updatedUser.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            updatedUser.setName(userDto.getName());
        }

        userRepository.save(updatedUser);

        return userMapper.userModelToUserDto(updatedUser);
    }


    @Transactional
    @Override
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден id: " + id));
        userRepository.delete(user);
    }

    private void throwIfEmailNotUnique(UserDto userDto) {
        for (User user : userRepository.findAll()) {
            if (user != null && user.getEmail() != null &&
                user.getEmail().equals(userDto.getEmail())) {
                throw new ValidationException("Email уже используется: " + userDto.getEmail());
            }
        }
    }
}
