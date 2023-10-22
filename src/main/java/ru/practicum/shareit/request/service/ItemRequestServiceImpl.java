package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestFullDto addItemRequest(long userId, ItemRequestCreateDto itemRequestCreateDto) {
        checkUserById(userId);
        User requestor = userRepository.findById(userId).get();
        ItemRequest itemRequest = ItemRequestMapper.INSTANCE.toItemRequestFromItemRequestCreateDto(itemRequestCreateDto);
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());
        return ItemRequestMapper.INSTANCE.toItemRequestFullDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestFullDto> getItemRequestsByRequestorId(long requestorId) {
        checkUserById(requestorId);
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequestorId(requestorId);
        return getItemsByRequestIn(itemRequests);
    }

    @Override
    public List<ItemRequestFullDto> getItemRequestsAll(long userId, Pageable pageable) {
        checkUserById(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequestorIdNot(userId);
        return getItemsByRequestIn(itemRequests);
    }

    @Override
    public ItemRequestFullDto getItemRequestById(long userId, long itemRequestId) {
        checkUserById(userId);
        checkRequestById(itemRequestId);
        return itemRequestRepository.findById(itemRequestId)
                .map(itemRequest -> {
                    itemRequest.setItems(itemRepository.findItemsByRequestId(itemRequest.getId()));
                    return ItemRequestMapper.INSTANCE.toItemRequestFullDto(itemRequest);
                }).get();
    }

    private void checkUserById(long userId) {
        if (!userRepository.existsById(userId)) {
            String errorMessage = String.format("Пользователь id %s не найден", userId);
            throw new NotFoundException(errorMessage);
        }
    }

    private void checkRequestById(long requestId) {
        if (!itemRequestRepository.existsById(requestId)) {
            String errorMessage = String.format("Запрос вещи id %s не найден", requestId);
            throw new NotFoundException(errorMessage);
        }
    }

    private List<ItemRequestFullDto> getItemsByRequestIn(List<ItemRequest> itemRequests) {
        Map<ItemRequest, List<Item>> collect = itemRepository.findItemsByRequestIn(itemRequests)
                .stream()
                .collect(Collectors.groupingBy(Item::getRequest, Collectors.toList()));
        itemRequests.forEach(itemRequest ->
                itemRequest.setItems(collect.getOrDefault(itemRequest, Collections.emptyList()))
        );
        return ItemRequestMapper.INSTANCE.toItemRequestsFullDto(itemRequests);
    }
}
