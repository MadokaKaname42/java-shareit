package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private EntityManager em;

    private User owner;
    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Alice");
        owner.setEmail("alice@mail.com");
        em.persist(owner);

        item1 = new Item();
        item1.setName("Drill");
        item1.setDescription("Powerful drill");
        item1.setAvailable(true);
        item1.setOwner(owner);
        em.persist(item1);

        item2 = new Item();
        item2.setName("Saw");
        item2.setDescription("Electric saw");
        item2.setAvailable(true);
        item2.setOwner(owner);
        em.persist(item2);

        item3 = new Item();
        item3.setName("Hammer");
        item3.setDescription("Steel hammer");
        item3.setAvailable(false); // не доступен
        item3.setOwner(owner);
        em.persist(item3);

        em.flush();
    }

    @Test
    void findAllByOwnerIdShouldReturnItems() {
        List<Item> items = itemRepository.findAllByOwnerId(owner.getId());

        assertThat(items).hasSize(3)
                .extracting(Item::getName)
                .containsExactlyInAnyOrder("Drill", "Saw", "Hammer");
    }

    @Test
    void findBySearchTextShouldReturnAvailableItemsMatchingText() {
        List<Item> items = itemRepository.findBySearchText("drill");

        assertThat(items).hasSize(1)
                .extracting(Item::getName)
                .containsExactly("Drill");

        List<Item> items2 = itemRepository.findBySearchText("saw");
        assertThat(items2).hasSize(1)
                .extracting(Item::getName)
                .containsExactly("Saw");

        // проверяем, что unavailable item не возвращается
        List<Item> items3 = itemRepository.findBySearchText("hammer");
        assertThat(items3).isEmpty();
    }

    @Test
    void findAllByItemRequestIdInOrderByIdShouldReturnItems() {
        // создаём ItemRequest
        ItemRequest request1 = new ItemRequest();
        request1.setDescription("Request 1");
        request1.setRequestor(owner);
        em.persist(request1);

        ItemRequest request2 = new ItemRequest();
        request2.setDescription("Request 2");
        request2.setRequestor(owner);
        em.persist(request2);

        // присваиваем ItemRequest объектам Item
        item1.setItemRequest(request1);
        item2.setItemRequest(request2);
        item3.setItemRequest(request1);

        em.flush();

        List<Item> items = itemRepository.findAllByItemRequestIdInOrderById(List.of(request1.getId()));

        assertThat(items).hasSize(2)
                .extracting(Item::getId)
                .containsExactlyInAnyOrder(item1.getId(), item3.getId());
    }

}

