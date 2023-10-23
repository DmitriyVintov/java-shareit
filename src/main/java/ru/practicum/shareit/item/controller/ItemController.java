package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.Marker;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentFullDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                  @RequestParam(required = false, defaultValue = "50") @Min(1) Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemDto> items = itemService.getItems(userId, pageable);
        log.info(String.format("Поступил запрос на получение всех вещей: %s", items));
        return items;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @PathVariable long itemId) {
        log.info(String.format("Поступил запрос на получение вещи id %s", itemId));
        return itemService.getItemById(itemId, userId);
    }

    @Validated(Marker.OnCreate.class)
    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @Valid @RequestBody ItemDto itemDto) {
        log.info(String.format("Поступил запрос от пользователя id %s на создание вещи: %s", userId, itemDto));
        return itemService.addItem(userId, itemDto);
    }

    @Validated(Marker.OnUpdate.class)
    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @Valid @RequestBody ItemDto itemDto, @PathVariable long itemId) {
        log.info(String.format("Поступил запрос от пользователя id %s на обновление вещи id %s: %s", userId, itemId, itemDto));
        return itemService.updateItem(userId, itemDto, itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @RequestParam String text,
                                     @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                     @RequestParam(required = false, defaultValue = "50") @Min(1) Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        log.info(String.format("Поступил запрос на поиск вещи по ключевому слову: %s", text));
        return itemService.search(userId, text, pageable);
    }

    @PostMapping("/{itemId}/comment")
    public CommentFullDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable long itemId,
                                     @Valid @RequestBody CommentCreateDto commentCreateDto) {
        log.info(String.format("Поступил запрос от пользователя id %s на создание комментария для вещи id %s: %s", userId, itemId, commentCreateDto));
        return itemService.addComment(userId, itemId, commentCreateDto);
    }
}