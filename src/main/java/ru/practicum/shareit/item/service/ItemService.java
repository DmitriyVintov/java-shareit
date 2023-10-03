package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentFullDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getItems(long userId);

    ItemDto getItemById(long itemId, long userId);

    ItemDto addItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, ItemDto itemDto, long itemId);

    CommentFullDto addComment(long userId, long itemId, CommentCreateDto commentCreateDto);

    void deleteItem(long userId, long itemId);

    List<ItemDto> search(long userId, String text);
}