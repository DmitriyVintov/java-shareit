package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.exception.Marker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@Validated
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotNull(groups = Marker.OnCreate.class, message = "Имя пользователя должно быть заполнено")
    private String name;
    @Email
    @NotNull(groups = Marker.OnCreate.class, message = "Email должен быть заполнен")
    private String email;
}