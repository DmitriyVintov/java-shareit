package ru.practicum.shareit.item.dto;

import lombok.*;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exception.Marker;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class ItemDto {
    private Long id;
    @NotBlank(groups = Marker.OnCreate.class, message = "Название вещи должно быть заполнено")
    private String name;
    @NotNull(groups = Marker.OnCreate.class, message = "Описание вещи должен быть заполнено")
    private String description;
    @NotNull(groups = Marker.OnCreate.class, message = "Статус доступности вещи должен быть заполнен")
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