package ru.practicum.shareit.request.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Validated
public class ItemRequest {
    private Integer id;
    private String description;
    private User requestor;
    @PastOrPresent
    private LocalDateTime created;
}
