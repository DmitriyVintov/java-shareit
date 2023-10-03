package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;

import java.util.List;

public interface BookingService {
    BookingFullDto addBooking(long userId, BookingCreateDto bookingCreateDto);

    BookingFullDto approvedOrRejectedBooking(long userId, long bookingId, boolean approved);

    BookingFullDto getBookingById(long userId, long bookingId);

    List<BookingFullDto> getBookingsByBookerId(long userId, String state);

    List<BookingFullDto> getAllBookingsForItemsByOwnerId(long userId, String state);
}
