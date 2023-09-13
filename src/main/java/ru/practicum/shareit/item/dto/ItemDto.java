package ru.practicum.shareit.item.dto;

import lombok.Data;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exception.Marker;
import ru.practicum.shareit.item.validate.NullOrNotBlank;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.NotNull;

@Data
@Validated
public class ItemDto {
    private Long id;
    @NullOrNotBlank
    private String name;
    @NotNull(groups = Marker.OnCreate.class)
    private String description;
    @NotNull(groups = Marker.OnCreate.class)
    private Boolean available;
    private UserDto owner;
//    private final Integer request;

    public ItemDto(Long id, String name, String description, Boolean available, UserDto owner) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.owner = owner;
    }
}
