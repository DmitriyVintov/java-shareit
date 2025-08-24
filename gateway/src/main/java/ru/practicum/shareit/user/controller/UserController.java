package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.exception.Marker;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {
    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        ResponseEntity<Object> users = userClient.getUsers();
        log.info(String.format("Поступил запрос на получение всех пользователей: %s", users));
        return users;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUsersById(
            @PathVariable long userId) {
        log.info(String.format("Поступил запрос на получение пользователя id %s", userId));
        return userClient.getUserById(userId);
    }

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public ResponseEntity<Object> addUser(
            @Valid @RequestBody UserDto userDto) {
        log.info(String.format("Поступил запрос на создание пользователя: %s", userDto));
        return userClient.addUser(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(
            @PathVariable long userId,
            @Valid @RequestBody UserDto userDto) {
        log.info(String.format("Поступил запрос на обновление пользователя id %s: %s", userId, userDto));
        return userClient.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(
            @PathVariable long userId) {
        log.info(String.format("Поступил запрос на удаление пользователя id %s", userId));
        userClient.deleteUser(userId);
    }
}