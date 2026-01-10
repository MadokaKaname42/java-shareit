package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindById() {
        User user = new User(null, "Alice", "alice@mail.ru");
        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
    }

    @Test
    void findAllReturnsSavedUsers() {
        User user1 = userRepository.save(new User(null, "Bob", "bob@mail.ru"));
        User user2 = userRepository.save(new User(null, "John", "john@mail.ru"));

        assertThat(userRepository.findAll()).containsExactlyInAnyOrder(user1, user2);
    }

    @Test
    void deleteByIdRemovesUser() {
        User user = userRepository.save(new User(null, "Eve", "eve@mail.ru"));

        userRepository.deleteById(user.getId());

        Optional<User> found = userRepository.findById(user.getId());
        assertThat(found).isNotPresent();
    }

    @Test
    void existsByIdReturnsCorrectValue() {
        User user = userRepository.save(new User(null, "Mark", "mark@mail.ru"));

        boolean exists = userRepository.existsById(user.getId());
        assertThat(exists).isTrue();

        userRepository.deleteById(user.getId());

        exists = userRepository.existsById(user.getId());
        assertThat(exists).isFalse();
    }
}

