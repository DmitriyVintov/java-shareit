package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exception.Marker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@Validated
@Builder
public class UserDto {
    private Long id;
    @NotNull(groups = Marker.OnCreate.class)
    private String name;
    @Email
    @NotNull(groups = Marker.OnCreate.class)
    private String email;

    public UserDto(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}