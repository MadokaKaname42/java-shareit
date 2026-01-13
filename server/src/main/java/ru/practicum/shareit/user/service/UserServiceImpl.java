package ru.practicum.shareit.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
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
        User user = userMapper.userDtoToUserModel(userDto);

        try {
            return userMapper.userModelToUserDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }
    }

    @Transactional
    @Override
    public UserDto update(UserDto userDto, Long id) {
        User updatedUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден id: " + id));


        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            updatedUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            boolean emailUsedByAnotherUser =
                    userRepository.existsByEmailAndIdNot(
                            userDto.getEmail(), id
                    );

            if (emailUsedByAnotherUser) {
                throw new EmailAlreadyExistsException(userDto.getEmail());
            }

            updatedUser.setEmail(userDto.getEmail());
        }
        return userMapper.userModelToUserDto(userRepository.save(updatedUser));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден id: " + id));
        userRepository.delete(user);
    }
}
