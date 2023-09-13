package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public List<ItemDto> getItems(long userId) {
        return itemRepository.getItems().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(long itemId) {
        checkItemById(itemId);
        return ItemMapper.toItemDto(itemRepository.getItemById(itemId));
    }

    @Override
    public ItemDto addItem(long userId, ItemDto itemDto) {
        userService.checkUserById(userId);
        Item item = ItemMapper.toItem(itemDto);
        User owner = UserMapper.toUser(userService.getUserById(userId));
        owner.setId(userId);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.addItem(item));
    }

    @Override
    public ItemDto updateItem(long userId, ItemDto itemDto, long itemId) {
        userService.checkUserById(userId);
        checkItemByOwner(userId, itemId);
        Item item = ItemMapper.toItem(getItemById(itemId));
        item.setId(itemId);
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        User owner = UserMapper.toUser(userService.getUserById(userId));
        owner.setId(userId);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.updateItem(item));
    }

    @Override
    public void deleteItem(long userId, long itemId) {
        userService.checkUserById(userId);
        checkItemByOwner(userId, itemId);
        itemRepository.deleteItem(itemId);
    }

    @Override
    public List<ItemDto> search(long userId, String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.getItems().stream()
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .filter(item -> item.getAvailable().equals(true))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void checkItemById(long itemId) {
        itemRepository.getItems().stream()
                .filter(item -> item.getId() == itemId)
                .findFirst()
                .orElseThrow(() -> {
                    log.info(String.format("Вещь с id %s не найдена", itemId));
                    return new NotFoundException(String.format("Вещь с id %s не найдена", itemId));
                });
    }

    @Override
    public void checkItemByOwner(long userId, long itemId) {
        itemRepository.getItems().stream()
                .filter(item -> item.getId() == itemId)
                .filter(item -> item.getOwner().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> {
                    log.info(String.format("У пользователя id %s вещь id %s не найдена",userId, itemId));
                    return new NotFoundException(String.format("У пользователя id %s вещь id %s не найдена",userId, itemId));
                });
    }
}
