package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1, user2;
    private ItemRequest req1, req2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setName("Alice");
        user1.setEmail("alice@mail.com");

        user2 = new User();
        user2.setName("Bob");
        user2.setEmail("bob@mail.com");

        userRepository.saveAll(List.of(user1, user2));

        req1 = new ItemRequest();
        req1.setRequestor(user1);
        req1.setDescription("Request1");

        req2 = new ItemRequest();
        req2.setRequestor(user2);
        req2.setDescription("Request2");

        requestRepository.saveAll(List.of(req1, req2));
    }

    @Test
    void testFindByRequestorIdOrderByCreatedDesc() {
        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(user1.getId());
        assertThat(requests).hasSize(1).first().extracting("description").isEqualTo("Request1");
    }

    @Test
    void testFindAllByRequestorIdNotOrderByCreatedDesc() {
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(user1.getId());
        assertThat(requests).hasSize(1).first().extracting("description").isEqualTo("Request2");
    }
}

