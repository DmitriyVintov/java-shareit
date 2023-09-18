package ru.practicum.shareit.booking.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.Future;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@RequiredArgsConstructor
@Validated
public class Booking {
    private Integer id;
    @PastOrPresent
    private LocalDateTime start;
    @Future
    private LocalDateTime end;
    private ItemDto item;
    private UserDto booker;
    private StatusBooking status;
}
