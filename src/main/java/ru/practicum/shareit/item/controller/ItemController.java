package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.Marker;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> get(@RequestHeader("X-Sharer-User-Id") long userId) {
        List<ItemDto> items = itemService.getItems(userId);
        log.info(String.format("Поступил запрос на получение всех вещей: %s", items));
        return items;
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable long itemId) {
        log.info(String.format("Поступил запрос на получение вещи id %s", itemId));
        return itemService.getItemById(itemId);
    }

    @Validated(Marker.OnCreate.class)
    @PostMapping
    public ItemDto add(@RequestHeader("X-Sharer-User-Id") Long userId,
                    @Valid @RequestBody ItemDto itemDto) {
        log.info(String.format("Поступил запрос от пользователя id %s на создание вещи: %s", userId, itemDto));
        return itemService.addItem(userId, itemDto);
    }

    @Validated(Marker.OnUpdate.class)
    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @Valid @RequestBody ItemDto itemDto, @PathVariable long itemId) {
        log.info(String.format("Поступил запрос от пользователя id %s на обновление вещи id %s: %s", userId, itemId, itemDto));
        return itemService.updateItem(userId, itemDto, itemId);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @PathVariable long itemId) {
        log.info(String.format("Поступил запрос на удаление у пользователя id %s вещи id %s", userId, itemId));
        itemService.deleteItem(userId, itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestHeader("X-Sharer-User-Id") long userId,
                                @RequestParam String text) {
        log.info(String.format("Поступил запрос на поиск вещи по ключевому слову: %s", text));
        return itemService.search(userId, text);
    }
}