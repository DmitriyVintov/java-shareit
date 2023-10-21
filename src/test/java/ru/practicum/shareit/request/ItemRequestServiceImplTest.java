package ru.practicum.shareit.request;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    private ItemRequestService itemRequestService;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    private final EasyRandom random = new EasyRandom();

    @BeforeEach
    void setUp() {
        itemRequestService = new ItemRequestServiceImpl(itemRequestRepository, userRepository, itemRepository);
    }

    @Test
    @DisplayName("Создание запроса на добавление вещи")
    void addItemRequest() {
        User user = random.nextObject(User.class);
        ItemRequestCreateDto itemRequestCreateDto = random.nextObject(ItemRequestCreateDto.class);
        ItemRequest itemRequest = ItemRequestMapper.INSTANCE.toItemRequestFromItemRequestCreateDto(itemRequestCreateDto);
        ItemRequestFullDto itemRequestFullDto = ItemRequestMapper.INSTANCE.toItemRequestFullDto(itemRequest);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(Mockito.any(ItemRequest.class))).thenReturn(itemRequest);

        assertEquals(itemRequestFullDto, itemRequestService.addItemRequest(user.getId(), itemRequestCreateDto));
    }

    @Test
    @DisplayName("Получение ошибки при создании запроса на добавление вещи, если пользователь не найден")
    void shouldThrowExceptionWhenAddItemRequestIfUserDoesNotExist() {
        User user = random.nextObject(User.class);
        ItemRequestCreateDto itemRequestCreateDto = random.nextObject(ItemRequestCreateDto.class);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemRequestService.addItemRequest(user.getId(), itemRequestCreateDto));
    }

    @Test
    @DisplayName("Получение списка своих запросов на добавление вещи")
    void getItemRequestsByOwnerId() {
        User requestor = random.nextObject(User.class);
        ItemRequest itemRequest = random.nextObject(ItemRequest.class);
        itemRequest.setRequestor(requestor);
        Item item = random.nextObject(Item.class);
        item.setRequest(itemRequest);
        List<Item> items = List.of(item);
        itemRequest.setItems(items);
        List<ItemRequest> itemRequests = List.of(itemRequest);
        List<ItemRequestFullDto> itemRequestsFullDto = ItemRequestMapper.INSTANCE.toItemRequestsFullDto(itemRequests);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findItemsByRequestId(itemRequest.getId())).thenReturn(items);
        when(itemRequestRepository.findAllByRequestorId(Mockito.anyLong())).thenReturn(itemRequests);

        assertEquals(itemRequestsFullDto, itemRequestService.getItemRequestsByRequestorId(requestor.getId()));
    }

    @Test
    @DisplayName("Получение списка запросов на добавление вещи, созданных другими пользователями")
    void getItemRequestsAll() {
        User requestor = random.nextObject(User.class);
        ItemRequest itemRequest = random.nextObject(ItemRequest.class);
        itemRequest.setRequestor(requestor);
        Item item = random.nextObject(Item.class);
        item.setRequest(itemRequest);
        List<Item> items = List.of(item);
        itemRequest.setItems(items);
        List<ItemRequest> itemRequests = List.of(itemRequest);
        List<ItemRequestFullDto> itemRequestsFullDto = ItemRequestMapper.INSTANCE.toItemRequestsFullDto(itemRequests);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findItemsByRequestId(itemRequest.getId())).thenReturn(items);
        when(itemRequestRepository.findAllByRequestorIdNot(Mockito.anyLong())).thenReturn(itemRequests);

        assertEquals(itemRequestsFullDto, itemRequestService.getItemRequestsAll(1L, Pageable.ofSize(3)));
    }

    @Test
    @DisplayName("Получение запроса на добавление вещи по id")
    void getItemRequestById() {
        User requestor = random.nextObject(User.class);
        ItemRequest itemRequest = random.nextObject(ItemRequest.class);
        itemRequest.setRequestor(requestor);
        Item item = random.nextObject(Item.class);
        item.setRequest(itemRequest);
        List<Item> items = List.of(item);
        itemRequest.setItems(items);
        ItemRequestFullDto itemRequestFullDto = ItemRequestMapper.INSTANCE.toItemRequestFullDto(itemRequest);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRequestRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findItemsByRequestId(itemRequest.getId())).thenReturn(items);
        when(itemRequestRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(itemRequest));

        assertEquals(itemRequestFullDto, itemRequestService.getItemRequestById(requestor.getId(), itemRequest.getId()));
    }

    @Test
    @DisplayName("Получение ошибки при получении запроса на добавление вещи по id, если запрос не найден")
    void shouldThrowExceptionWhenGetItemRequestByIdIfItemRequestDoesNotExist() {
        User requestor = random.nextObject(User.class);
        ItemRequest itemRequest = random.nextObject(ItemRequest.class);
        itemRequest.setRequestor(requestor);
        Item item = random.nextObject(Item.class);
        item.setRequest(itemRequest);
        List<Item> items = List.of(item);
        itemRequest.setItems(items);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRequestRepository.existsById(Mockito.anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemRequestService.getItemRequestById(requestor.getId(), itemRequest.getId()));
    }
}