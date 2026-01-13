package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.comments.dto.CommentDtoPost;
import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.comments.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoPost;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(null, "Owner", "owner@mail.ru"));
        item = itemRepository.save(
                new Item(null, owner, "Item1", "Description", true, null)
        );
    }

    @AfterEach
    void tearDown() {
        commentRepository.deleteAll();
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createItem_shouldSaveItem() {
        ItemDtoPost postDto = new ItemDtoPost("Drill", "Cordless drill", true, null);

        ItemDto result = itemService.create(postDto, owner.getId());

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Drill");
        assertThat(itemRepository.findById(result.getId())).isPresent();
    }

    @Test
    void updateItem_shouldUpdateFields() {
        ItemDtoPost update = new ItemDtoPost("Updated", "Updated desc", false, null);

        ItemDto result = itemService.update(update, item.getId(), owner.getId());

        assertThat(result.getName()).isEqualTo("Updated");
        assertThat(result.getAvailable()).isFalse();
    }

    @Test
    void getById_shouldReturnItemWithComments() {
        commentRepository.save(
                new Comment(null, "Nice", item, owner, LocalDateTime.now())
        );

        ItemDto dto = itemService.getById(item.getId());

        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getText()).isEqualTo("Nice");
    }

    @Test
    void search_shouldFindByText() {
        List<ItemDto> result = itemService.search("desc");

        assertThat(result).hasSize(1);
    }

    @Test
    void search_blankText_shouldReturnEmpty() {
        List<ItemDto> result = itemService.search("   ");

        assertThat(result).isEmpty();
    }

    @Test
    void createComment_shouldSaveComment_whenUserHadBooking() {
        User booker = userRepository.save(
                new User(null, "Booker", "booker@mail.ru")
        );

        bookingRepository.save(
                Booking.builder()
                        .item(item)
                        .booker(booker)
                        .start(LocalDateTime.now().minusDays(2))
                        .end(LocalDateTime.now().minusDays(1))
                        .status(BookingStatus.APPROVED)
                        .build()
        );

        CommentDtoPost post = new CommentDtoPost("Great!");

        CommentDto result = itemService.createComment(item.getId(), booker.getId(), post);

        assertThat(result.getText()).isEqualTo("Great!");
        assertThat(commentRepository.findAll()).hasSize(1);
    }

    @Test
    void findItemsByRequestId_shouldReturnLinkedItems() {
        User requester = userRepository.save(
                new User(null, "Requester", "req@mail.ru")
        );

        ItemRequest request = itemRequestRepository.save(
                new ItemRequest(null, "Need item", requester, LocalDateTime.now(), null)
        );

        Item requestedItem = itemRepository.save(
                new Item(null, owner, "Chair", "Wooden", true, request)
        );

        List<Item> found = itemRepository.findAllByItemRequestIdInOrderById(
                List.of(request.getId())
        );

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Chair");
    }
}

