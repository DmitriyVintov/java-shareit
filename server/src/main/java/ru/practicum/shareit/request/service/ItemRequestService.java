package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestFullDto addItemRequest(long userId, ItemRequestCreateDto itemRequestCreateDto);

    List<ItemRequestFullDto> getItemRequestsByRequestorId(long ownerId);

    List<ItemRequestFullDto> getItemRequestsAll(long userId, Pageable pageable);

    ItemRequestFullDto getItemRequestById(long userId, long itemRequestId);
}
