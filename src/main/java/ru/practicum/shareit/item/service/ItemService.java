package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getItems(long userId);

    ItemDto getItemById(long itemId);

    ItemDto addItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, ItemDto itemDto, long itemId);

    void deleteItem(long userId, long itemId);

    List<ItemDto> search(long userId, String text);

    void checkItemById(long userId);

    void checkItemByOwner(long userId, long itemId);
}