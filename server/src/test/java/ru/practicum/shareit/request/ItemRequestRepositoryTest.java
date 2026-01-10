package ru.practicum.shareit.request.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private EntityManager em;

    private User user1;
    private User user2;
    private ItemRequest request1;
    private ItemRequest request2;
    private ItemRequest request3;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setName("Alice");
        user1.setEmail("alice@example.com");
        em.persist(user1);

        user2 = new User();
        user2.setName("Bob");
        user2.setEmail("bob@example.com");
        em.persist(user2);

        request1 = new ItemRequest();
        request1.setDescription("Request 1");
        request1.setRequestor(user1);
        request1.setCreated(LocalDateTime.now().minusDays(2));
        em.persist(request1);

        request2 = new ItemRequest();
        request2.setDescription("Request 2");
        request2.setRequestor(user1);
        request2.setCreated(LocalDateTime.now().minusDays(1));
        em.persist(request2);

        request3 = new ItemRequest();
        request3.setDescription("Request 3");
        request3.setRequestor(user2);
        request3.setCreated(LocalDateTime.now());
        em.persist(request3);

        em.flush();
    }

    @Test
    void findByRequestorIdOrderByCreatedDesc_ShouldReturnRequestsOfUser1() {
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(user1.getId());

        assertThat(requests).hasSize(2)
                .extracting(ItemRequest::getId)
                .containsExactly(request2.getId(), request1.getId()); // сортировка по created DESC
    }

    @Test
    void findAllByRequestorIdNotOrderByCreatedDesc_ShouldReturnRequestsNotFromUser1() {
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(user1.getId());

        assertThat(requests).hasSize(1)
                .extracting(ItemRequest::getId)
                .containsExactly(request3.getId());
    }
}

