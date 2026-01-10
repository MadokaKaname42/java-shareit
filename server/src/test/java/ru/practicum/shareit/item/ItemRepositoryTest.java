package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    private User user;
    private ItemRequest request;
    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
        // Создаём и сохраняем пользователя
        user = User.builder()
                .name("Alice")
                .email("alice@mail.com")
                .build();
        user = userRepository.save(user);

        // Создаём и сохраняем запрос
        request = ItemRequest.builder()
                .description("Need item")
                .requestor(user)
                .build();
        request = requestRepository.save(request);

        // Создаём и сохраняем три вещи
        item1 = Item.builder()
                .name("Item1")
                .description("Description1")
                .available(true)
                .owner(user)
                .itemRequest(request)
                .build();

        item2 = Item.builder()
                .name("Special Item")
                .description("Something special")
                .available(true)
                .owner(user)
                .build();

        item3 = Item.builder()
                .name("Another Item")
                .description("Another description")
                .available(true)
                .owner(user)
                .itemRequest(request)
                .build();

        itemRepository.saveAll(List.of(item1, item2, item3));
    }

    @Test
    void findAllByOwnerIdShouldReturnItems() {
        List<Item> items = itemRepository.findAllByOwnerId(user.getId());

        assertThat(items).hasSize(3)
                .extracting(Item::getName)
                .containsExactlyInAnyOrder("Item1", "Special Item", "Another Item");
    }

    @Test
    void findBySearchTextShouldReturnMatchingItems() {
        List<Item> items = itemRepository.findBySearchText("special");

        assertThat(items).hasSize(1)
                .extracting(Item::getName)
                .containsExactly("Special Item");
    }

    @Test
    void findAllByItemRequestIdInOrderByIdShouldReturnItems() {
        List<Item> items = itemRepository.findAllByItemRequestIdInOrderById(List.of(request.getId()));

        assertThat(items).hasSize(2)
                .extracting(Item::getId)
                .containsExactlyInAnyOrder(item1.getId(), item3.getId());
    }
}


