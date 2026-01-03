package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users;

    private long id;

    public UserRepositoryImpl() {
        users = new HashMap<>();
        id = 0;
    }

    @Override
    public List<User> findAll() {
        log.info("Репозиторий возвратил список пользователей.");
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(Long id) {
        log.info("Поиск пользователя с id = {}", id);
        User user = users.get(id);
        return user == null ? Optional.empty() : Optional.of(user);
    }

    @Override
    public User create(User user) {
        user.setId(generateId());
        users.put(user.getId(), user);
        log.info("Создан пользователь id = {}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        log.info("Обновление пользователя id = {}", user.getId());
        return user;
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
        log.info("Удален пользователь id = {}", id);
    }

    private long generateId() {
        return id++;
    }
}
