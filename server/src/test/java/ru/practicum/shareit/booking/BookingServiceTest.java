package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    BookingRepository bookingRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;
    @Mock
    BookingMapper bookingMapper;

    @InjectMocks
    BookingService bookingService;

    User owner;
    User booker;
    Item item;
    Booking booking;
    BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@mail.com");
        booker = new User(2L, "Booker", "booker@mail.com");

        item = new Item(10L, owner, "Drill", "description", true, null);

        booking = new Booking(100L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item,
                booker,
                BookingStatus.WAITING);

        bookingDto = BookingDto.builder()
                .id(100L)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void create_validBooking_returnsDto() {
        BookingShortDto dto = BookingShortDto.builder()
                .itemId(item.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingMapper.bookingShortDtoToBookingModel(dto)).thenReturn(booking);
        when(bookingMapper.bookingModelToBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.create(dto, booker.getId());

        assertThat(result).isEqualTo(bookingDto);
        verify(bookingRepository).save(booking);
    }

    @Test
    void create_userNotFound_throwsNotFound() {
        BookingShortDto dto = BookingShortDto.builder().itemId(item.getId())
                .start(booking.getStart()).end(booking.getEnd()).build();
        when(userRepository.findById(booker.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(dto, booker.getId()));
    }

    @Test
    void create_itemNotFound_throwsNotFound() {
        BookingShortDto dto = BookingShortDto.builder().itemId(item.getId())
                .start(booking.getStart()).end(booking.getEnd()).build();
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(dto, booker.getId()));
    }

    @Test
    void create_ownerBooksOwnItem_throwsNotFound() {
        BookingShortDto dto = BookingShortDto.builder().itemId(item.getId())
                .start(booking.getStart()).end(booking.getEnd()).build();
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.create(dto, owner.getId()));
    }

    @Test
    void create_itemNotAvailable_throwsBadRequest() {
        item.setAvailable(false);
        BookingShortDto dto = BookingShortDto.builder().itemId(item.getId())
                .start(booking.getStart()).end(booking.getEnd()).build();
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class, () -> bookingService.create(dto, booker.getId()));
    }

    @Test
    void create_endBeforeStart_throwsBadRequest() {
        BookingShortDto dto = BookingShortDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingMapper.bookingShortDtoToBookingModel(dto)).thenReturn(
                new Booking(null, dto.getStart(), dto.getEnd(), item, booker, null));

        assertThrows(BadRequestException.class, () -> bookingService.create(dto, booker.getId()));
    }

    @Test
    void approve_ownerApproves_setsApproved() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingMapper.bookingModelToBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.approve(booking.getId(), owner.getId(), true);

        assertThat(result).isEqualTo(bookingDto);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approve_ownerRejects_setsRejected() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingMapper.bookingModelToBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.approve(booking.getId(), owner.getId(), false);

        assertThat(result).isEqualTo(bookingDto);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approve_notOwner_throwsBadRequest() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        assertThrows(BadRequestException.class, () -> bookingService.approve(booking.getId(), booker.getId(), true));
    }

    @Test
    void approve_alreadyApproved_throwsBadRequest() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        assertThrows(BadRequestException.class, () -> bookingService.approve(booking.getId(), owner.getId(), true));
    }

    @Test
    void getById_ownerOrBooker_returnsDto() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingMapper.bookingModelToBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result1 = bookingService.getById(booking.getId(), owner.getId());
        BookingDto result2 = bookingService.getById(booking.getId(), booker.getId());

        assertThat(result1).isEqualTo(bookingDto);
        assertThat(result2).isEqualTo(bookingDto);
    }

    @Test
    void getById_otherUser_throwsNotFound() {
        User stranger = new User(99L, "Stranger", "s@mail.com");
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getById(booking.getId(), stranger.getId()));
    }

    @Test
    void getAllByUser_eachSupportedState_returnsList() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingMapper.bookingModelToBookingDto(booking)).thenReturn(bookingDto);

        when(bookingRepository.findAllByBooker(eq(booker), any(Sort.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerAndStartBeforeAndEndAfter(eq(booker), any(), any(), any(Sort.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerAndEndBefore(eq(booker), any(), any(Sort.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerAndStartAfter(eq(booker), any(), any(Sort.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerAndStatusEquals(eq(booker), eq(BookingStatus.WAITING), any(Sort.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByBookerAndStatusEquals(eq(booker), eq(BookingStatus.REJECTED), any(Sort.class)))
                .thenReturn(List.of(booking));

        String[] supportedStates = {"ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"};
        for (String state : supportedStates) {
            List<BookingDto> result = bookingService.getAllByUser(booker.getId(), state);
            assertThat(result).hasSize(1);
        }

        assertThrows(BadRequestException.class,
                () -> bookingService.getAllByUser(booker.getId(), "UNKNOWN"));
    }


    @Test
    void getAllByOwner_eachSupportedState_returnsList() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingMapper.bookingModelToBookingDto(booking)).thenReturn(bookingDto);

        when(bookingRepository.findAllByItemOwner(eq(owner), any(Sort.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByItemOwnerAndStartBeforeAndEndAfter(eq(owner), any(), any(), any(Sort.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByItemOwnerAndEndBefore(eq(owner), any(), any(Sort.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByItemOwnerAndStartAfter(eq(owner), any(), any(Sort.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByItemOwnerAndStatusEquals(eq(owner), eq(BookingStatus.WAITING), any(Sort.class)))
                .thenReturn(List.of(booking));
        when(bookingRepository.findAllByItemOwnerAndStatusEquals(eq(owner), eq(BookingStatus.REJECTED), any(Sort.class)))
                .thenReturn(List.of(booking));

        String[] supportedStates = {"ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"};
        for (String state : supportedStates) {
            List<BookingDto> result = bookingService.getAllByOwner(owner.getId(), state);
            assertThat(result).hasSize(1);
        }

        assertThrows(BadRequestException.class,
                () -> bookingService.getAllByOwner(owner.getId(), "UNKNOWN"));
    }

    @Test
    void getAllByOwner_userNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.getAllByOwner(1L, "ALL"));
    }
}