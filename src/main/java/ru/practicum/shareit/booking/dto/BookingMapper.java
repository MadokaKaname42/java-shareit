package ru.practicum.shareit.booking.dto;

import org.mapstruct.Mapper;
import ru.practicum.shareit.booking.model.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    Booking bookingDtoToBookingModel(BookingDto bookingDto);

    Booking bookingShortDtoToBookingModel(BookingShortDto bookingShortDto);

    Booking bookingForItemDtoToBookingModel(BookingForItemDto bookingForItemDto);

    BookingDto bookingModelToBookingDto(Booking booking);
}
