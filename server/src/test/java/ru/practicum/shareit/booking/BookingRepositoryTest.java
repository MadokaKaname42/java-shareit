package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager em;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = em.persist(new User(null, "Owner", "owner@mail.com"));
        booker = em.persist(new User(null, "Booker", "booker@mail.com"));
        item = em.persist(new Item(null, owner, "Item", "Description", true, null));

        booking = em.persist(new Booking(
                null,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                item,
                booker,
                BookingStatus.WAITING
        ));
    }

    @Test
    void findAllByBooker_returnsList() {
        List<Booking> result = bookingRepository.findAllByBooker(booker, Sort.by(Sort.Direction.DESC, "start"));
        assertThat(result).hasSize(1).contains(booking);
    }

    @Test
    void findAllByBookerAndStatusEquals_returnsList() {
        List<Booking> result = bookingRepository.findAllByBookerAndStatusEquals(booker, BookingStatus.WAITING,
                Sort.by("start"));
        assertThat(result).hasSize(1).contains(booking);
    }

    @Test
    void findAllByBookerAndStartBeforeAndEndAfter_returnsList() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = bookingRepository.findAllByBookerAndStartBeforeAndEndAfter(booker,
                now.plusHours(1), now.minusHours(1), Sort.by("start"));
        assertThat(result).hasSize(1).contains(booking);
    }

    @Test
    void findAllByBookerAndEndBefore_returnsEmptyIfFuture() {
        List<Booking> result = bookingRepository.findAllByBookerAndEndBefore(booker,
                LocalDateTime.now().minusDays(2), Sort.by("start"));
        assertThat(result).isEmpty();
    }

    @Test
    void findAllByItemOwnerAndStatusEquals_returnsList() {
        List<Booking> result = bookingRepository.findAllByItemOwnerAndStatusEquals(owner, BookingStatus.WAITING,
                Sort.by("start"));
        assertThat(result).hasSize(1).contains(booking);
    }

    @Test
    void findAllByItemOwnerAndStartAfter_returnsEmptyIfPast() {
        List<Booking> result = bookingRepository.findAllByItemOwnerAndStartAfter(owner,
                LocalDateTime.now().plusDays(2), Sort.by("start"));
        assertThat(result).isEmpty();
    }
}

