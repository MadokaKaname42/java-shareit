package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    private final Sort sort = Sort.by(Sort.Direction.DESC, "start");

    @Transactional
    public BookingDto create(BookingShortDto bookingShortDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not possible create Booking - " +
                                                         "Not found User with Id " + userId));
        Item item = itemRepository.findById(bookingShortDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Not possible create Booking - " +
                                                         "Not found Item with Id " + bookingShortDto.getItemId()));
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Not possible create booking - " +
                                        "User cannot book a thing belonging to him");
        }
        if (!item.isAvailable()) {
            throw new BadRequestException("Not possible create Booking - " +
                                          "this item is not available");
        }
        Booking booking = bookingMapper.bookingShortDtoToBookingModel(bookingShortDto);
        if (booking.getEnd().isBefore(booking.getStart()) || booking.getEnd() == booking.getStart()) {
            throw new BadRequestException("Not possible create Booking - " +
                                          "the end date of the booking cannot be earlier than the start date of the booking");
        }
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking);

        return bookingMapper.bookingModelToBookingDto(booking);
    }

    @Transactional
    public BookingDto approve(Long bookingId, Long userId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Not possible create Booking - " +
                                                         "Not found Booking with Id " + bookingId));
        if (!userId.equals(booking.getItem().getOwner().getId())) {
            throw new BadRequestException("Not possible create booking - " +
                                          "Not found Booking with Id " + bookingId + " for user with an id " + userId);
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new BadRequestException("It is not possible to confirm the Booking - " +
                                          "the booking has already been confirmed or declined");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return bookingMapper.bookingModelToBookingDto(booking);
    }

    public List<BookingDto> getAllByOwner(Long userId, String state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found Bookings - " +
                                                         "there is no User with Id " + userId));
        List<Booking> bookingDtoList;
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }

        switch (bookingState) {
            case ALL:
                bookingDtoList = bookingRepository.findAllByItemOwner(user, sort);
                break;
            case CURRENT:
                bookingDtoList = bookingRepository.findAllByItemOwnerAndStartBeforeAndEndAfter(user,
                        LocalDateTime.now(), LocalDateTime.now(), sort);
                break;
            case PAST:
                bookingDtoList = bookingRepository.findAllByItemOwnerAndEndBefore(user,
                        LocalDateTime.now(), sort);
                break;
            case FUTURE:
                bookingDtoList = bookingRepository.findAllByItemOwnerAndStartAfter(user, LocalDateTime.now(), sort);
                break;
            case WAITING:
                bookingDtoList = bookingRepository.findAllByItemOwnerAndStatusEquals(user, BookingStatus.WAITING, sort);
                break;
            case REJECTED:
                bookingDtoList = bookingRepository.findAllByItemOwnerAndStatusEquals(user, BookingStatus.REJECTED, sort);
                break;
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }

        return bookingDtoList.stream()
                .map(bookingMapper::bookingModelToBookingDto)
                .collect(Collectors.toList());
    }


    public List<BookingDto> getAllByUser(Long userId, String state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found Bookings - " +
                                                         "Not found User with Id " + userId));
        List<Booking> bookingDtoList;
        switch (BookingState.valueOf(state)) {
            case ALL:
                bookingDtoList = bookingRepository.findAllByBooker(user, sort);
                break;
            case CURRENT:
                bookingDtoList = bookingRepository.findAllByBookerAndStartBeforeAndEndAfter(user,
                        LocalDateTime.now(), LocalDateTime.now(), sort);
                break;
            case PAST:
                bookingDtoList = bookingRepository.findAllByBookerAndEndBefore(user,
                        LocalDateTime.now(), sort);
                break;
            case FUTURE:
                bookingDtoList = bookingRepository.findAllByBookerAndStartAfter(user, LocalDateTime.now(), sort);
                break;
            case WAITING:
                bookingDtoList = bookingRepository.findAllByBookerAndStatusEquals(user, BookingStatus.WAITING, sort);
                break;
            case REJECTED:
                bookingDtoList = bookingRepository.findAllByBookerAndStatusEquals(user, BookingStatus.REJECTED, sort);
                break;
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }

        return bookingDtoList.stream().map(bookingMapper::bookingModelToBookingDto).collect(Collectors.toList());
    }

    public BookingDto getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("There is no Booking with Id " + bookingId));
        if (!userId.equals(booking.getBooker().getId()) && !userId.equals(booking.getItem().getOwner().getId())) {
            throw new NotFoundException("Only the Booking author can view the booking details " +
                                        "or the owner of Item");
        }

        return bookingMapper.bookingModelToBookingDto(booking);
    }
}
