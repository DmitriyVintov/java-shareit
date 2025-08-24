package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.exception.Marker;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getItems(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
            @RequestParam(required = false, defaultValue = "50") @Min(1) Integer size) {
        ResponseEntity<Object> items = itemClient.getItems(userId, from, size);
        log.info(String.format("Поступил запрос на получение всех вещей: %s", items));
        return items;
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable long itemId) {
        log.info(String.format("Поступил запрос на получение вещи id %s", itemId));
        return itemClient.getItemById(userId, itemId);
    }


    @PostMapping
    public ResponseEntity<Object> addItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Validated(Marker.OnCreate.class) @RequestBody ItemDto itemDto) {
        log.info(String.format("Поступил запрос от пользователя id %s на создание вещи: %s", userId, itemDto));
        return itemClient.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Validated(Marker.OnUpdate.class) @RequestBody ItemDto itemDto,
            @PathVariable long itemId) {
        log.info(String.format("Поступил запрос от пользователя id %s на обновление вещи id %s: %s", userId, itemId, itemDto));
        return itemClient.updateItem(userId, itemDto, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam String text,
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
            @RequestParam(required = false, defaultValue = "50") @Min(1) Integer size) {
        log.info(String.format("Поступил запрос на поиск вещи по ключевому слову: %s", text));
        return itemClient.search(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentCreateDto commentCreateDto) {
        log.info(String.format("Поступил запрос от пользователя id %s на создание комментария для вещи id %s: %s", userId, itemId, commentCreateDto));
        return itemClient.addComment(userId, itemId, commentCreateDto);
    }
}