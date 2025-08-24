package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentFullDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getItems(long ownerId, Pageable pageable);

    ItemDto getItemById(long itemId, long ownerId);

    ItemDto addItem(long ownerId, ItemDto itemDto);

    ItemDto updateItem(long ownerId, ItemDto itemDto, long itemId);

    CommentFullDto addComment(long userId, long itemId, CommentCreateDto commentCreateDto);

    List<ItemDto> search(long userId, String text, Pageable pageable);
}