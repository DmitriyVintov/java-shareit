package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.booking.validate.DateEndAfterDateStart;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
@DateEndAfterDateStart
public class BookingCreateDto {
    private Long id;
    @NotNull(message = "Время начала бронирования должно быть заполнено")
    @FutureOrPresent(message = "Время начала бронирования не должно быть в прошлом")
    private LocalDateTime start;
    @NotNull(message = "Время окончания бронирования должно быть заполнено")
    private LocalDateTime end;
    @NotNull(message = "Id вещи должно быть заполнено")
    private Long itemId;
}
