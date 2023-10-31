package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentFullDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> getItems(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "50") Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemDto> items = itemService.getItems(userId, pageable);
        log.info(String.format("Поступил запрос на получение всех вещей: %s", items));
        return items;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable long itemId) {
        log.info(String.format("Поступил запрос на получение вещи id %s", itemId));
        return itemService.getItemById(itemId, userId);
    }

    @PostMapping
    public ItemDto addItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemDto itemDto) {
        log.info(String.format("Поступил запрос от пользователя id %s на создание вещи: %s", userId, itemDto));
        return itemService.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemDto itemDto,
            @PathVariable long itemId) {
        log.info(String.format("Поступил запрос от пользователя id %s на обновление вещи id %s: %s", userId, itemId, itemDto));
        return itemService.updateItem(userId, itemDto, itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam String text,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "50") Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        log.info(String.format("Поступил запрос на поиск вещи по ключевому слову: %s", text));
        return itemService.search(userId, text, pageable);
    }

    @PostMapping("/{itemId}/comment")
    public CommentFullDto addComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody CommentCreateDto commentCreateDto) {
        log.info(String.format("Поступил запрос от пользователя id %s на создание комментария для вещи id %s: %s", userId, itemId, commentCreateDto));
        return itemService.addComment(userId, itemId, commentCreateDto);
    }
}