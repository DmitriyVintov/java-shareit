package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getUsers() {
        List<UserDto> users = userService.getUsers();
        log.info(String.format("Поступил запрос на получение всех пользователей: %s", users));
        return users;
    }

    @GetMapping("/{userId}")
    public UserDto getUsersById(
            @PathVariable long userId) {
        log.info(String.format("Поступил запрос на получение пользователя id %s", userId));
        return userService.getUserById(userId);
    }

    @PostMapping
    public UserDto addUser(
            @RequestBody UserDto userDto) {
        log.info(String.format("Поступил запрос на создание пользователя: %s", userDto));
        return userService.addUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(
            @PathVariable long userId,
            @RequestBody UserDto userDto) {
        log.info(String.format("Поступил запрос на обновление пользователя id %s: %s", userId, userDto));
        return userService.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(
            @PathVariable long userId) {
        log.info(String.format("Поступил запрос на удаление пользователя id %s", userId));
        userService.deleteUser(userId);
    }
}