package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> addItemRequest(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDto) {
        log.info(String.format("Поступил запрос от пользователя id %s на создание запроса вещи: %s", userId, itemRequestCreateDto));
        return itemRequestClient.addItemRequest(userId, itemRequestCreateDto);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequestsByRequestorId(
            @RequestHeader("X-Sharer-User-Id") Long requestorId) {
        log.info(String.format("Поступил запрос от пользователя id %s на получение запросов вещей", requestorId));
        return itemRequestClient.getItemRequestsByRequestorId(requestorId);
    }

    @GetMapping("/all")
    @Validated
    public ResponseEntity<Object> getItemRequestsAll(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
            @RequestParam(required = false, defaultValue = "50") @Min(1) Integer size) {
        log.info(String.format("Поступил запрос от пользователя id %s на получение всех запросов вещей от других пользователей", userId));
        return itemRequestClient.getItemRequestsAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable long requestId) {
        log.info(String.format("Поступил запрос от пользователя id %s на получение запроса вещи id %s", userId, requestId));
        return itemRequestClient.getItemRequestById(userId, requestId);
    }
}
