package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.Marker;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> get() {
        List<UserDto> users = userService.getUsers();
        log.info(String.format("Поступил запрос на получение всех пользователей: %s", users));
        return users;
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable long userId) {
        log.info(String.format("Поступил запрос на получение пользователя id %s", userId));
        return userService.getUserById(userId);
    }

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public UserDto add(@Valid @RequestBody UserDto userDto) {
        log.info(String.format("Поступил запрос на создание пользователя: %s", userDto));
        return userService.addUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable long userId,
                          @Valid @RequestBody UserDto userDto) {
        log.info(String.format("Поступил запрос на обновление пользователя id %s: %s", userId, userDto));
        return userService.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        log.info(String.format("Поступил запрос на удаление пользователя id %s", userId));
        userService.deleteUser(userId);
    }
}