package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestFullDto addItemRequest(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemRequestCreateDto itemRequestCreateDto) {
        log.info(String.format("Поступил запрос от пользователя id %s на создание запроса вещи: %s", userId, itemRequestCreateDto));
        return itemRequestService.addItemRequest(userId, itemRequestCreateDto);
    }

    @GetMapping
    public List<ItemRequestFullDto> getItemRequestsByRequestorId(
            @RequestHeader("X-Sharer-User-Id") Long requestorId) {
        log.info(String.format("Поступил запрос от пользователя id %s на получение запросов вещей", requestorId));
        return itemRequestService.getItemRequestsByRequestorId(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestFullDto> getItemRequestsAll(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "50") Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        log.info(String.format("Поступил запрос от пользователя id %s на получение всех запросов вещей от других пользователей", userId));
        return itemRequestService.getItemRequestsAll(userId, pageable);
    }

    @GetMapping("/{requestId}")
    public ItemRequestFullDto getItemRequestById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable long requestId) {
        log.info(String.format("Поступил запрос от пользователя id %s на получение запроса вещи id %s", userId, requestId));
        return itemRequestService.getItemRequestById(userId, requestId);
    }
}
