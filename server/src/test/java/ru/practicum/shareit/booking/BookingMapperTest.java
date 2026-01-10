package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookingMapperTest {

    @Autowired
    private BookingMapper mapper;

    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        User owner = new User(1L, "Alice", "alice@mail.com");
        booker = new User(2L, "Bob", "bob@mail.com");
        item = new Item(10L, owner, "Drill", "Powerful drill", true, null);
    }

    @Test
    void bookingModelToBookingDtoShouldMapAllFields() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        Booking booking = new Booking(100L, start, end, item, booker, BookingStatus.WAITING);

        BookingDto dto = mapper.bookingModelToBookingDto(booking);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getStart()).isEqualTo(start);
        assertThat(dto.getEnd()).isEqualTo(end);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);

        assertThat(dto.getItem()).isNotNull();
        assertThat(dto.getItem().getId()).isEqualTo(10L);

        assertThat(dto.getBooker()).isNotNull();
        assertThat(dto.getBooker().getId()).isEqualTo(2L);
    }

    @Test
    void mapBookingsToBookingForItemsDtoShouldMapList() {
        Booking booking1 = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2), item, booker, BookingStatus.APPROVED);
        Booking booking2 = new Booking(2L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), item, booker, BookingStatus.WAITING);

        List<BookingForItemDto> dtos = mapper.mapBookingsToBookingForItemsDto(List.of(booking1, booking2));

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getId()).isEqualTo(1L);
        assertThat(dtos.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void bookingShortDtoToBookingModelShouldMapFields() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingShortDto shortDto = new BookingShortDto(50L, start, end, 10L);

        Booking booking = mapper.bookingShortDtoToBookingModel(shortDto);

        assertThat(booking).isNotNull();
        assertThat(booking.getId()).isEqualTo(50L);
        assertThat(booking.getStart()).isEqualTo(start);
        assertThat(booking.getEnd()).isEqualTo(end);
        assertThat(booking.getStatus()).isNull();
    }
}

