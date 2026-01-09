package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoPost;
import ru.practicum.shareit.booking.validation.ValidBookingState;
import ru.practicum.shareit.common.web.HeaderConstants;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestBody @Valid BookingDtoPost bookingDtoPost) {

        log.info("BookingDtoShort before send: {}", bookingDtoPost);

        log.info("Creating booking {}, userId={}", bookingDtoPost, userId);
        return bookingClient.bookItem(userId, bookingDtoPost);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateBookingStatus(
            @PathVariable Long bookingId,
            @RequestParam Boolean approved,
            @RequestHeader(HeaderConstants.USER_ID) Long ownerId) {

        log.info("Patch /bookings/{}?approved={} by user {}", bookingId, approved, ownerId);
        return bookingClient.updateBookingStatus(bookingId, approved, ownerId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(
            @RequestHeader(HeaderConstants.USER_ID) long userId,
            @PathVariable Long bookingId) {

        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByBooker(
            @RequestHeader(HeaderConstants.USER_ID) Long bookerId,
            @RequestParam(defaultValue = "ALL") @ValidBookingState String state) {

        log.info("Get /bookings?state={} for booker {}", state, bookerId);
        return bookingClient.getBookingsByBooker(bookerId, state);
    }

    // Получение всех бронирований для owner
    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwner(
            @RequestHeader(HeaderConstants.USER_ID) Long ownerId,
            @RequestParam(defaultValue = "ALL")
            @ValidBookingState String state) {

        log.info("Get /bookings/owner?state={} for owner {}", state, ownerId);
        return bookingClient.getBookingsByOwner(ownerId, state);
    }
}
