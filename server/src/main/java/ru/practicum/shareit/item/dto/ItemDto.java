package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private UserDto owner;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private List<CommentFullDto> comments;
    private Long requestId;

    @Value
    public static class UserDto {
        long id;
        String name;
    }

    @Value
    public static class BookingDto {
        long id;
        long bookerId;
        LocalDateTime start;
        LocalDateTime end;
    }
}