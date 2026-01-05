package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.web.HeaderConstants;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@Valid @RequestBody BookingShortDto bookingShortDto,
                             @RequestHeader(HeaderConstants.USER_ID) Long userId) {
        return bookingService.create(bookingShortDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@PathVariable Long bookingId, @RequestHeader(HeaderConstants.USER_ID) Long userId,
                              @RequestParam Boolean approved) {
        return bookingService.approve(bookingId, userId, approved);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByOwner(@RequestHeader(HeaderConstants.USER_ID) Long userId,
                                          @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllByOwner(userId, state);
    }

    @GetMapping
    public List<BookingDto> getAllByUser(@RequestHeader(HeaderConstants.USER_ID) Long userId,
                                         @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllByUser(userId, state);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@PathVariable Long bookingId, @RequestHeader(HeaderConstants.USER_ID) Long userId) {
        return bookingService.getById(bookingId, userId);
    }
}
