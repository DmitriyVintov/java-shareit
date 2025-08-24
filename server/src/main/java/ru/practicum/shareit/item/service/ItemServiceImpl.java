package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public List<ItemDto> getItems(long ownerId, Pageable pageable) {
        LocalDateTime currentTime = LocalDateTime.now();
        return itemRepository.findAllByOwnerIdOrderById(ownerId, pageable).stream()
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
    public ItemDto getItemById(long itemId, long ownerId) {
        LocalDateTime currentTime = LocalDateTime.now();
        return ItemMapper.INSTANCE.toItemDto(itemRepository.findById(itemId)
                .map(item -> {
                    List<Booking> lastBooking = bookingRepository.findLastBookingByOwnerId(itemId, ownerId, StatusBooking.REJECTED, currentTime);
                    if (!lastBooking.isEmpty()) {
                        item.setLastBooking(lastBooking.get(0));
                    } else {
                        item.setLastBooking(null);
                    }
                    List<Booking> nextBooking = bookingRepository.findNextBookingByOwnerId(itemId, ownerId, StatusBooking.REJECTED, currentTime);
                    if (!nextBooking.isEmpty()) {
                        item.setNextBooking(nextBooking.get(0));
                    } else {
                        item.setNextBooking(null);
                    }
                    item.setComments(commentRepository.findAllByItemId(itemId));
                    return item;
                }).orElseThrow(() -> {
                    String errorMessage = String.format("Вещь id %s не найдена", itemId);
                    return new NotFoundException(errorMessage);
                }));
    }

    @Override
    public ItemDto addItem(long ownerId, ItemDto itemDto) {
        checkUserById(ownerId);
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        User owner = userRepository.findById(ownerId).get();
        item.setOwner(owner);
        Long requestId = itemDto.getRequestId();
        if (requestId != null) {
            checkExistItemRequest(requestId);
            ItemRequest itemRequest = itemRequestRepository.findById(requestId).get();
            item.setRequest(itemRequest);
        }
        return ItemMapper.INSTANCE.toItemDto(itemRepository.save(item));
    }

    @Override
    public CommentFullDto addComment(long userId, long itemId, CommentCreateDto commentCreateDto) {
        LocalDateTime currentTime = LocalDateTime.now();
        checkBookingByItemAndUserAndStatusAndPast(userId, itemId);
        User author = userRepository.findById(userId).get();
        Item item = itemRepository.findById(itemId).get();
        Comment comment = CommentMapper.INSTANCE.toComment(commentCreateDto);
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(currentTime);
        return CommentMapper.INSTANCE.toCommentFullDto(commentRepository.save(comment));
    }

    @Override
    public ItemDto updateItem(long ownerId, ItemDto itemDto, long itemId) {
        checkUserById(ownerId);
        checkItemByOwner(ownerId, itemId);
        Item item = itemRepository.findByIdAndOwnerId(itemId, ownerId).get();
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
        User owner = userRepository.findById(ownerId).get();
        owner.setId(ownerId);
        item.setOwner(owner);
        return ItemMapper.INSTANCE.toItemDto(itemRepository.save(item));
    }

    @Override
    public List<ItemDto> search(long userId, String text, Pageable pageable) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemRepository.search(text, pageable).stream()
                .map(ItemMapper.INSTANCE::toItemDto)
                .collect(Collectors.toList());
    }

    private void checkUserById(long userId) {
        if (!userRepository.existsById(userId)) {
            String errorMessage = String.format("Пользователь id %s не найден", userId);
            throw new NotFoundException(errorMessage);
        }
    }

    private void checkExistItemRequest(Long requestId) {
        if (!itemRequestRepository.existsById(requestId)) {
            String errorMessage = String.format("Запрос на создание id %s не найден", requestId);
            throw new NotFoundException(errorMessage);
        }
    }

    private void checkItemByOwner(long userId, long itemId) {
        if (!itemRepository.existsByIdAndOwnerId(itemId, userId)) {
            String errorMessage = String.format("У пользователя id %s вещь id %s не найдена", userId, itemId);
            throw new NotFoundException(errorMessage);
        }
    }

    private void checkBookingByItemAndUserAndStatusAndPast(long userId, long itemId) {
        LocalDateTime currentTime = LocalDateTime.now();
        if (!bookingRepository.existsBookingByBookerIdAndItemIdAndStatusAndStartBefore(userId, itemId, StatusBooking.APPROVED, currentTime)) {
            String errorMessage = String.format("Нет бронирований с вещью %s и пользователем %s", itemId, userId);
            throw new ValidationException(errorMessage);
        }
    }
}