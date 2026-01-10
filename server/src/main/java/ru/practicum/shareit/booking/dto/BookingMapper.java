package ru.practicum.shareit.booking.dto;

import org.mapstruct.Mapper;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    Booking bookingShortDtoToBookingModel(BookingShortDto bookingShortDto);

    BookingDto bookingModelToBookingDto(Booking booking);

    List<BookingForItemDto> mapBookingsToBookingForItemsDto(List<Booking> booking);
}
