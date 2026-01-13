package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveUser_shouldPersistAndGenerateId() {
        User user = User.builder()
                .name("Alice")
                .email("alice@mail.com")
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Alice");
        assertThat(saved.getEmail()).isEqualTo("alice@mail.com");
    }

    @Test
    void findById_whenUserExists_shouldReturnUser() {
        User user = userRepository.save(
                new User(null, "Bob", "bob@mail.com")
        );

        Optional<User> found = userRepository.findById(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("bob@mail.com");
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        userRepository.save(new User(null, "User1", "u1@mail.com"));
        userRepository.save(new User(null, "User2", "u2@mail.com"));

        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void delete_shouldRemoveUserFromDatabase() {
        User user = userRepository.save(
                new User(null, "DeleteMe", "delete@mail.com")
        );

        userRepository.deleteById(user.getId());

        Optional<User> found = userRepository.findById(user.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void saveUser_withDuplicateEmail_shouldThrowException() {
        userRepository.save(
                new User(null, "Alice", "duplicate@mail.com")
        );

        User duplicate = new User(null, "Bob", "duplicate@mail.com");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(duplicate)
        );
    }
}