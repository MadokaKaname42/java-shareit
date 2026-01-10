package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
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
        user = User.builder().name("Alice").email("alice@mail.com").build();
        user = userRepository.save(user);

        request = ItemRequest.builder().description("Need item").requestor(user).build();
        request = requestRepository.save(request);

        item1 = Item.builder()
                .name("Item1")
                .description("Desc1")
                .available(true)
                .owner(user)
                .itemRequest(request)
                .build();

        item2 = Item.builder()
                .name("Special Item2")
                .description("Desc2 special")
                .available(true)
                .owner(user)
                .build();

        item3 = Item.builder()
                .name("Item3")
                .description("Desc3")
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
                .containsExactlyInAnyOrder(item1.getName(), item2.getName(), item3.getName());
    }

    @Test
    void findBySearchTextShouldReturnMatchingItems() {
        List<Item> items = itemRepository.findBySearchText("special");

        assertThat(items).hasSize(1)
                .first()
                .extracting(Item::getName)
                .isEqualTo(item2.getName());
    }

    @Test
    void findAllByItemRequestIdInOrderByIdShouldReturnItems() {
        List<Item> items = itemRepository.findAllByItemRequestIdInOrderById(List.of(request.getId()));

        assertThat(items).hasSize(2)
                .extracting(Item::getId)
                .containsExactlyInAnyOrder(item1.getId(), item3.getId());
    }
}
