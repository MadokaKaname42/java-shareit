package ru.practicum.shareit.item;

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

    private User user;
    private ItemRequest request;
    private Item item1, item2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Alice");
        user.setEmail("alice@mail.com");

        request = new ItemRequest();
        request.setRequestor(user);
        request.setDescription("Need item");

        item1 = new Item();
        item1.setName("Item1");
        item1.setDescription("Description1");
        item1.setOwner(user);
        item1.setAvailable(true);
        item1.setItemRequest(request);

        item2 = new Item();
        item2.setName("Special Item");
        item2.setDescription("Something special");
        item2.setOwner(user);
        item2.setAvailable(true);

        itemRepository.save(item1);
        itemRepository.save(item2);
    }

    @Test
    void testFindAllByOwnerId() {
        List<Item> items = itemRepository.findAllByOwnerId(user.getId());
        assertThat(items).hasSize(2).extracting(Item::getName)
                .containsExactlyInAnyOrder("Item1", "Special Item");
    }

    @Test
    void testFindBySearchText() {
        List<Item> items = itemRepository.findBySearchText("special");
        assertThat(items).hasSize(1).first().extracting(Item::getName).isEqualTo("Special Item");
    }

    @Test
    void testFindAllByItemRequestIdInOrderById() {
        List<Item> items = itemRepository.findAllByItemRequestIdInOrderById(List.of(request.getId()));
        assertThat(items).hasSize(1).first().extracting(Item::getName).isEqualTo("Item1");
    }
}


