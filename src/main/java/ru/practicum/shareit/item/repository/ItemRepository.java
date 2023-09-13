package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    List<Item> getItems();

    Item getItemById(long itemId);

    Item updateItem(Item item);

    void deleteItem(long itemId);

    Item addItem(Item item);
}
