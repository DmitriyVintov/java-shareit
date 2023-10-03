package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentFullDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public List<ItemDto> getItems(long userId) {
        LocalDateTime currentTime = LocalDateTime.now();
        return itemRepository.findAllByOwnerIdOrderById(userId).stream()
                .map(item -> {
                    item.setLastBooking(bookingRepository.findFirstBookingByItemIdAndStatusNotAndStartBeforeOrderByStartDesc(
                            item.getId(), StatusBooking.REJECTED, currentTime
                    ).orElse(null));
                    item.setNextBooking(bookingRepository.findFirstBookingByItemIdAndStatusNotAndStartAfterOrderByStart(
                            item.getId(), StatusBooking.REJECTED, currentTime
                    ).orElse(null));
                    item.setComments(commentRepository.findAllByItemId(item.getId()));
                    return ItemMapper.INSTANCE.toItemDto(item);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(long itemId, long userId) {
        LocalDateTime currentTime = LocalDateTime.now();
        return ItemMapper.INSTANCE.toItemDto(itemRepository.findById(itemId)
                .map(item -> {
                    List<Booking> lastBooking = bookingRepository.findLastBookingByOwnerId(itemId, userId, StatusBooking.REJECTED, currentTime);
                    if (!lastBooking.isEmpty()) {
                        item.setLastBooking(lastBooking.get(0));
                    } else {
                        item.setLastBooking(null);
                    }
                    List<Booking> nextBooking = bookingRepository.findNextBookingByOwnerId(itemId, userId, StatusBooking.REJECTED, currentTime);
                    if (!nextBooking.isEmpty()) {
                        item.setNextBooking(nextBooking.get(0));
                    } else {
                        item.setNextBooking(null);
                    }
                    item.setComments(commentRepository.findAllByItemId(itemId));
                    return item;
                }).orElseThrow(() -> {
                    String errorMessage = String.format("Вещь id %s не найдена", itemId);
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                }));
    }

    @Override
    public ItemDto addItem(long userId, ItemDto itemDto) {
        userService.getUserById(userId);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        User owner = UserMapper.INSTANCE.toUser(userService.getUserById(userId));
        owner.setId(userId);
        item.setOwner(owner);
        return ItemMapper.INSTANCE.toItemDto(itemRepository.save(item));
    }

    @Override
    public CommentFullDto addComment(long userId, long itemId, CommentCreateDto commentCreateDto) {
        LocalDateTime currentTime = LocalDateTime.now();
        checkBookingByItemAndUserAndStatusAndPast(userId, itemId);
        User author = UserMapper.INSTANCE.toUser(userService.getUserById(userId));
        Item item = ItemMapper.INSTANCE.toItem(getItemById(itemId, userId));
        Comment comment = CommentMapper.INSTANCE.toComment(commentCreateDto);
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(currentTime);
        return CommentMapper.INSTANCE.toCommentFullDto(commentRepository.save(comment));
    }

    @Override
    public ItemDto updateItem(long userId, ItemDto itemDto, long itemId) {
        userService.getUserById(userId);
        checkItemByOwner(userId, itemId);
        Item item = ItemMapper.INSTANCE.toItem(getItemById(itemId, userId));
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
        User owner = UserMapper.INSTANCE.toUser(userService.getUserById(userId));
        owner.setId(userId);
        item.setOwner(owner);
        return ItemMapper.INSTANCE.toItemDto(itemRepository.save(item));
    }

    @Override
    public void deleteItem(long userId, long itemId) {
        userService.getUserById(userId);
        checkItemByOwner(userId, itemId);
        itemRepository.delete(ItemMapper.INSTANCE.toItem(getItemById(itemId, userId)));
    }

    @Override
    public List<ItemDto> search(long userId, String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper.INSTANCE::toItemDto)
                .collect(Collectors.toList());
    }

    private void checkItemByOwner(long userId, long itemId) {
        itemRepository.findAllByIdAndOwnerId(itemId, userId).stream()
                .findFirst()
                .orElseThrow(() -> {
                    String errorMessage = String.format("У пользователя id %s вещь id %s не найдена", userId, itemId);
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                });
    }

    private void checkBookingByItemAndUserAndStatusAndPast(long userId, long itemId) {
        LocalDateTime currentTime = LocalDateTime.now();
        if (!bookingRepository.existsBookingByBookerIdAndItemIdAndStatusAndStartBefore(userId, itemId, StatusBooking.APPROVED, currentTime)) {
            String errorMessage = String.format("Нет бронирований с вещью %s и пользователем %s", itemId, userId);
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }
}