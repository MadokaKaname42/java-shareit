package ru.practicum.shareit.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(null, "Owner", "owner@mail.ru"));
        booker = userRepository.save(new User(null, "Booker", "booker@mail.ru"));

        item = itemRepository.save(
                new Item(null, owner, "Drill", "Cordless drill", true, null)
        );
    }

    @AfterEach
    void tearDown() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void create_shouldSaveBooking() {
        BookingShortDto dto = BookingShortDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingDto booking = bookingService.create(dto, booker.getId());

        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(booking.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(booking.getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void create_shouldThrowIfBookerIsOwner() {
        BookingShortDto dto = BookingShortDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThrows(NotFoundException.class,
                () -> bookingService.create(dto, owner.getId()));
    }

    @Test
    void create_shouldThrowIfEndBeforeStart() {
        BookingShortDto dto = BookingShortDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        assertThrows(BadRequestException.class,
                () -> bookingService.create(dto, booker.getId()));
    }

    @Test
    void approve_shouldApproveBooking() {
        BookingShortDto dto = BookingShortDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingDto booking = bookingService.create(dto, booker.getId());

        BookingDto approved = bookingService.approve(
                booking.getId(), owner.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approve_shouldRejectBooking() {
        BookingShortDto dto = BookingShortDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingDto booking = bookingService.create(dto, booker.getId());

        BookingDto rejected = bookingService.approve(
                booking.getId(), owner.getId(), false);

        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approve_shouldThrowIfNotOwner() {
        BookingShortDto dto = BookingShortDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingDto booking = bookingService.create(dto, booker.getId());

        assertThrows(BadRequestException.class,
                () -> bookingService.approve(booking.getId(), booker.getId(), true));
    }

    @Test
    void getById_shouldReturnForBooker() {
        BookingDto booking = bookingService.create(
                BookingShortDto.builder()
                        .itemId(item.getId())
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(2))
                        .build(),
                booker.getId()
        );

        BookingDto found = bookingService.getById(booking.getId(), booker.getId());

        assertThat(found.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getById_shouldThrowIfUnauthorized() {
        BookingDto booking = bookingService.create(
                BookingShortDto.builder()
                        .itemId(item.getId())
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(2))
                        .build(),
                booker.getId()
        );

        User stranger = userRepository.save(
                new User(null, "Stranger", "stranger@mail.ru")
        );

        assertThrows(NotFoundException.class,
                () -> bookingService.getById(booking.getId(), stranger.getId()));
    }

    @Test
    void getAllByUser_shouldReturnBookings() {
        bookingService.create(
                BookingShortDto.builder()
                        .itemId(item.getId())
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(2))
                        .build(),
                booker.getId()
        );

        List<BookingDto> bookings =
                bookingService.getAllByUser(booker.getId(), "ALL");

        assertThat(bookings).hasSize(1);
    }

    @Test
    void getAllByOwner_shouldReturnBookings() {
        bookingService.create(
                BookingShortDto.builder()
                        .itemId(item.getId())
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(2))
                        .build(),
                booker.getId()
        );

        List<BookingDto> bookings =
                bookingService.getAllByOwner(owner.getId(), "ALL");

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(item.getId());
    }
}