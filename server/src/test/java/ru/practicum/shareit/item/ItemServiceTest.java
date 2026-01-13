package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.comments.dto.CommentDtoPost;
import ru.practicum.shareit.comments.dto.CommentMapper;
import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.comments.repository.CommentRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoPost;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private BookingMapper bookingMapper;

    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@mail.com");

        item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);
        item.setOwner(owner);
    }

    @Test
    void createItem_ShouldReturnItemDto() {
        ItemDtoPost postDto = new ItemDtoPost();
        postDto.setName("Drill");
        postDto.setDescription("Powerful drill");
        postDto.setAvailable(true);

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemMapper.itemDtoPostToItemModel(postDto)).thenReturn(item);
        when(itemMapper.itemModelToItemDto(item)).thenReturn(new ItemDto(1L, "Drill", "Powerful drill", true, null, null, emptyList()));

        ItemDto result = itemService.create(postDto, owner.getId());

        assertNotNull(result);
        assertEquals("Drill", result.getName());
        verify(itemRepository).save(item);
    }

    @Test
    void createItem_UserNotFound_ShouldThrow() {
        ItemDtoPost postDto = new ItemDtoPost();
        when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> itemService.create(postDto, owner.getId()));
        assertTrue(ex.getMessage().contains("Пользователь не найден"));
    }

    @Test
    void updateItem_ShouldUpdateFields() {
        ItemDtoPost updateDto = new ItemDtoPost();
        updateDto.setName("New Drill");
        updateDto.setDescription("Updated description");

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemMapper.itemModelToItemDto(item)).thenReturn(new ItemDto(1L, "New Drill", "Updated description", true, null, null, emptyList()));

        ItemDto result = itemService.update(updateDto, item.getId(), owner.getId());

        assertEquals("New Drill", result.getName());
        assertEquals("Updated description", result.getDescription());
    }

    @Test
    void updateItem_NotOwner_ShouldThrow() {
        ItemDtoPost updateDto = new ItemDtoPost();
        Long otherUserId = 2L;

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        NotFoundException ex = assertThrows(NotFoundException.class, () -> itemService.update(updateDto, item.getId(), otherUserId));
        assertTrue(ex.getMessage().contains("Не возможно обновить вещь"));
    }

    @Test
    void getById_ShouldReturnItemDtoWithComments() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemMapper.itemModelToItemDto(item)).thenReturn(new ItemDto(1L, "Drill", "Powerful drill", true, null, null, emptyList()));
        when(commentMapper.toDto(anyList())).thenReturn(emptyList());

        ItemDto result = itemService.getById(item.getId());

        assertEquals(1L, result.getId());
        verify(commentMapper).toDto(anyList());
    }

    @Test
    void createComment_UserHasNotBooked_ShouldThrow() {
        CommentDtoPost commentDtoPost = new CommentDtoPost();
        commentDtoPost.setText("Good item");

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByBookerIdAndItemIdAndStatusEqualsAndEndIsBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(emptyList());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> itemService.createComment(item.getId(), owner.getId(), commentDtoPost));
        assertTrue(ex.getMessage().contains("Item has not been rented"));
    }

    @Test
    void search_WithText_ShouldReturnMatchingItems() {
        List<Item> items = List.of(item);
        when(itemRepository.findBySearchText("drill")).thenReturn(items);
        when(itemMapper.itemModelToItemDto(item)).thenReturn(
                new ItemDto(1L, "Drill", "Powerful drill", true, null, null, emptyList())
        );

        List<ItemDto> result = itemService.search("drill");

        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
    }

    @Test
    void search_WithBlankText_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.search("  ");
        assertTrue(result.isEmpty());
    }

    @Test
    void delete_ExistingItem_ShouldCallRepositoryDelete() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        itemService.delete(item.getId());

        verify(itemRepository).delete(item);
    }

    @Test
    void delete_NonExistingItem_ShouldThrow() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> itemService.delete(item.getId()));
        assertTrue(ex.getMessage().contains("Вещь не найдена"));
    }

    @Test
    void createItem_WithRequestId_ShouldSetRequest() {
        ItemDtoPost postDto = new ItemDtoPost();
        postDto.setName("Drill");
        postDto.setDescription("Powerful drill");
        postDto.setAvailable(true);
        postDto.setRequestId(10L);

        ItemRequest request = new ItemRequest();
        request.setId(10L);

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(itemMapper.itemDtoPostToItemModel(postDto)).thenReturn(item);
        when(itemMapper.itemModelToItemDto(item))
                .thenReturn(new ItemDto(1L, "Drill", "Powerful drill", true, null, null, emptyList()));

        ItemDto result = itemService.create(postDto, owner.getId());

        assertNotNull(result);
        verify(itemRequestRepository).findById(10L);
        verify(itemRepository).save(item);
    }


    @Test
    void updateItem_AllFieldsNull_ShouldNotChangeItem() {
        ItemDtoPost updateDto = new ItemDtoPost();

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemMapper.itemModelToItemDto(item))
                .thenReturn(new ItemDto(1L, "Drill", "Powerful drill", true, null, null, emptyList()));

        ItemDto result = itemService.update(updateDto, item.getId(), owner.getId());

        assertEquals("Drill", result.getName());
        assertEquals("Powerful drill", result.getDescription());
    }


    @Test
    void getAll_NoBookings_ShouldReturnItemsWithoutBookingInfo() {
        when(itemRepository.findAllByOwnerId(owner.getId())).thenReturn(List.of(item));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByBookerAndStatusEquals(any(), any(), any()))
                .thenReturn(emptyList());
        when(itemMapper.mapItemsToDto(any()))
                .thenReturn(List.of(new ItemDto(1L, "Drill", "Desc", true, null, null, emptyList())));

        List<ItemDto> result = itemService.getAll(owner.getId());

        assertEquals(1, result.size());
        assertNull(result.get(0).getLastBooking());
        assertNull(result.get(0).getNextBooking());
    }


    @Test
    void getAll_WithPastAndFutureBookings_ShouldSetLastAndNext() {
        ItemDto dto = new ItemDto(1L, "Drill", "Desc", true, null, null, emptyList());

        BookingForItemDto past = new BookingForItemDto(
                1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), 1L, 2L
        );
        BookingForItemDto future = new BookingForItemDto(
                2L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 1L, 2L
        );

        when(itemRepository.findAllByOwnerId(owner.getId())).thenReturn(List.of(item));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemMapper.mapItemsToDto(any())).thenReturn(List.of(dto));
        when(bookingRepository.findAllByBookerAndStatusEquals(any(), any(), any()))
                .thenReturn(List.of());
        when(bookingMapper.mapBookingsToBookingForItemsDto(any()))
                .thenReturn(List.of(past, future));

        List<ItemDto> result = itemService.getAll(owner.getId());

        assertNotNull(result.get(0).getLastBooking());
        assertNotNull(result.get(0).getNextBooking());
    }


    @Test
    void createComment_WhenUserBooked_ShouldCreateComment() {
        CommentDtoPost post = new CommentDtoPost();
        post.setText("Nice item");

        Booking booking = new Booking();
        booking.setEnd(LocalDateTime.now().minusDays(1));

        Comment comment = new Comment();
        CommentDto dto = new CommentDto(1L, "Nice item", "User", LocalDateTime.now());

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByBookerIdAndItemIdAndStatusEqualsAndEndIsBefore(
                anyLong(), anyLong(), any(), any()))
                .thenReturn(List.of(booking));
        when(commentMapper.toComment(any(), any(), any())).thenReturn(comment);
        when(commentMapper.commentModelToCommentDto(comment)).thenReturn(dto);

        CommentDto result = itemService.createComment(item.getId(), owner.getId(), post);

        assertEquals("Nice item", result.getText());
        verify(commentRepository).save(comment);
    }

}
