package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;

import java.util.List;

@Mapper(uses = {ItemMapper.class})
public interface BookingMapper {
    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    BookingFullDto toBookingFullDto(Booking booking);

    @Mapping(source = "itemId", target = "item.id")
    Booking toBookingFromBookingCreateDto(BookingCreateDto bookingCreateDto);

    List<BookingFullDto> toBookingsFullDto(List<Booking> bookings);
}
