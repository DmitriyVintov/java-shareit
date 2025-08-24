package ru.practicum.shareit.item;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
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
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Disabled
class ItemServiceImplTest {
    private ItemService itemService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    private final EasyRandom random = new EasyRandom();

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);
    }

    @Test
    @DisplayName("Получение вещей по id владельца")
    void getItems() {
        User owner = random.nextObject(User.class);
        Booking lastBooking = random.nextObject(Booking.class);
        Booking nextBooking = random.nextObject(Booking.class);
        List<Comment> comments = random.objects(Comment.class, 2).collect(Collectors.toList());
        List<Item> items = random.objects(Item.class, 5)
                .peek(item -> {
                    item.setOwner(owner);
                    item.setLastBooking(lastBooking);
                    item.setNextBooking(nextBooking);
                    item.setComments(comments);
                })
                .collect(Collectors.toList());
        long ownerId = items.get(0).getOwner().getId();
        List<ItemDto> itemsDto = ItemMapper.INSTANCE.toItemsDto(items);

        when(itemRepository.findAllByOwnerIdOrderById(Mockito.anyLong(), Mockito.any(Pageable.class))).thenReturn(items);
        when(bookingRepository.findFirstBookingByItemIdAndStatusNotAndStartBeforeOrderByStartDesc(
                Mockito.anyLong(), Mockito.any(StatusBooking.class), Mockito.any(LocalDateTime.class))).thenReturn(Optional.ofNullable(lastBooking));
        when(bookingRepository.findFirstBookingByItemIdAndStatusNotAndStartAfterOrderByStart(
                Mockito.anyLong(), Mockito.any(StatusBooking.class), Mockito.any(LocalDateTime.class))).thenReturn(Optional.ofNullable(nextBooking));
        when(commentRepository.findAllByItemId(Mockito.anyLong())).thenReturn(comments);

        assertEquals(itemsDto, itemService.getItems(ownerId, Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение вещи по id вещи и по id владельца")
    void getItemById() {
        User owner = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        Booking lastBooking = random.nextObject(Booking.class);
        Booking nextBooking = random.nextObject(Booking.class);
        List<Comment> comments = random.objects(Comment.class, 2).collect(Collectors.toList());
        item.setOwner(owner);
        item.setLastBooking(lastBooking);
        item.setNextBooking(nextBooking);
        item.setComments(comments);
        ItemDto itemDto = ItemMapper.INSTANCE.toItemDto(item);

        when(itemRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findLastBookingByOwnerId(
                Mockito.anyLong(), Mockito.anyLong(), Mockito.any(StatusBooking.class), Mockito.any(LocalDateTime.class))).thenReturn(List.of(lastBooking));
        when(bookingRepository.findNextBookingByOwnerId(
                Mockito.anyLong(), Mockito.anyLong(), Mockito.any(StatusBooking.class), Mockito.any(LocalDateTime.class))).thenReturn(List.of(nextBooking));
        when(commentRepository.findAllByItemId(Mockito.anyLong())).thenReturn(comments);

        assertEquals(itemDto, itemService.getItemById(item.getId(), owner.getId()));
    }

    @Test
    @DisplayName("Получение ошибки при получении вещи по id вещи и по id владельца, когда вещь не найдена")
    void shouldThrowExceptionWhenGetItemByIdIfItemDoesNotExist() {
        when(itemRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemById(1L, 1L));
    }

    @Test
    @DisplayName("Создание вещи")
    void addItem() {
        Item item = random.nextObject(Item.class);
        User owner = random.nextObject(User.class);
        ItemRequest itemRequest = random.nextObject(ItemRequest.class);
        List<Comment> comments = random.objects(Comment.class, 2).collect(Collectors.toList());
        item.setOwner(owner);
        item.setComments(comments);
        item.setRequest(itemRequest);
        ItemDto itemDto = ItemMapper.INSTANCE.toItemDto(item);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.ofNullable(owner));
        when(itemRequestRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRequestRepository.findById(Mockito.anyLong())).thenReturn(Optional.ofNullable(itemRequest));
        when(itemRepository.save(Mockito.any(Item.class))).thenReturn(item);

        assertEquals(itemDto, itemService.addItem(item.getId(), itemDto));
    }

    @Test
    @DisplayName("Получение ошибки при создании вещи, когда владелец не найден")
    void shouldThrowExceptionWhenAddItemIfOwnerDoesNotExist() {
        ItemDto itemDto = random.nextObject(ItemDto.class);
        when(userRepository.existsById(Mockito.anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.addItem(1L, itemDto));
    }

    @Test
    @DisplayName("Получение ошибки при создании вещи, когда вещь не найдена")
    void shouldThrowExceptionWhenAddItemIfItemDoesNotExist() {
        ItemDto itemDto = random.nextObject(ItemDto.class);
        User owner = random.nextObject(User.class);
        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.ofNullable(owner));
        when(itemRequestRepository.existsById(Mockito.anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.addItem(1L, itemDto));
    }

    @Test
    @DisplayName("Создание комментария")
    void addComment() {
        Item item = random.nextObject(Item.class);
        User user = random.nextObject(User.class);
        CommentCreateDto commentCreateDto = random.nextObject(CommentCreateDto.class);
        Comment comment = CommentMapper.INSTANCE.toComment(commentCreateDto);
        List<Comment> comments = List.of(comment);
        item.setComments(comments);
        CommentFullDto commentFullDto = CommentMapper.INSTANCE.toCommentFullDto(comment);

        when(bookingRepository.existsBookingByBookerIdAndItemIdAndStatusAndStartBefore(
                Mockito.anyLong(), Mockito.anyLong(), Mockito.any(StatusBooking.class), Mockito.any(LocalDateTime.class))).thenReturn(true);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.save(Mockito.any(Comment.class))).thenReturn(comment);

        assertEquals(commentFullDto, itemService.addComment(user.getId(), item.getId(), commentCreateDto));
    }

    @Test
    @DisplayName("Получение ошибки при создании комментария, когда нет бронирования с данной вещью и пользователем")
    void shouldThrowExceptionWhenAddItemIfBookingWithCurrentItemAndCurrentUserDoesNotExist() {
        CommentCreateDto commentCreateDto = random.nextObject(CommentCreateDto.class);
        when(bookingRepository.existsBookingByBookerIdAndItemIdAndStatusAndStartBefore(
                Mockito.anyLong(), Mockito.anyLong(), Mockito.any(StatusBooking.class), Mockito.any(LocalDateTime.class))).thenReturn(false);

        assertThrows(ValidationException.class, () -> itemService.addComment(1L, 1L, commentCreateDto));
    }

    @Test
    @DisplayName("Обновление вещи")
    void updateItem() {
        Item item = random.nextObject(Item.class);
        User owner = random.nextObject(User.class);
        item.setOwner(owner);
        ItemDto itemDto = ItemMapper.INSTANCE.toItemDto(item);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.existsByIdAndOwnerId(Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findByIdAndOwnerId(Mockito.anyLong(), Mockito.anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.save(Mockito.any(Item.class))).thenReturn(item);

        assertEquals(itemDto, itemService.updateItem(owner.getId(), itemDto, item.getId()));
    }

    @Test
    @DisplayName("Обновление вещи, когда обновляемый объект с пустыми полями")
    void updateItemWhenItemWithEmptyFields() {
        Item item = random.nextObject(Item.class);
        User owner = random.nextObject(User.class);
        item.setOwner(owner);
        item.setName(null);
        item.setDescription(null);
        item.setAvailable(null);
        ItemDto itemDto = ItemMapper.INSTANCE.toItemDto(item);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.existsByIdAndOwnerId(Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findByIdAndOwnerId(Mockito.anyLong(), Mockito.anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.save(Mockito.any(Item.class))).thenReturn(item);

        assertEquals(itemDto, itemService.updateItem(owner.getId(), itemDto, item.getId()));
    }

    @Test
    @DisplayName("Получение ошибки при обновлении вещи, если пользователь не найден")
    void shouldThrowExceptionWhenUpdateItemIfUserDoesNotExist() {
        Item item = random.nextObject(Item.class);
        User owner = random.nextObject(User.class);
        ItemDto itemDto = ItemMapper.INSTANCE.toItemDto(item);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.updateItem(owner.getId(), itemDto, item.getId()));
    }

    @Test
    @DisplayName("Получение ошибки при обновлении вещи, если вещь не найдена")
    void shouldThrowExceptionWhenUpdateItemIfItemDoesNotExist() {
        Item item = random.nextObject(Item.class);
        User owner = random.nextObject(User.class);
        item.setOwner(owner);
        ItemDto itemDto = ItemMapper.INSTANCE.toItemDto(item);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.existsByIdAndOwnerId(Mockito.anyLong(), Mockito.anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.updateItem(owner.getId(), itemDto, item.getId()));
    }

    @Test
    @DisplayName("Получение списка вещей по поисковой фразе")
    void search() {
        String text = "text";
        ItemDto itemDto1 = random.nextObject(ItemDto.class);
        itemDto1.setComments(Collections.emptyList());
        itemDto1.setRequestId(null);
        itemDto1.setLastBooking(null);
        itemDto1.setNextBooking(null);
        itemDto1.setName("Название " + text);
        itemDto1.setDescription("Описание " + text);
        List<ItemDto> itemsDto = List.of(itemDto1);
        List<Item> items = ItemMapper.INSTANCE.toItems(itemsDto);

        when(itemRepository.search(Mockito.anyString(), Mockito.any(Pageable.class))).thenReturn(items);

        assertEquals(1, itemService.search(1L, "text", Pageable.ofSize(1)).size());
        assertEquals(itemDto1, itemService.search(1L, "text", Pageable.ofSize(1)).get(0));
    }

    @Test
    @DisplayName("Получение пустого списка при поиске, когда запрос пустой")
    void shouldGetEmptyListWhenRequestIsEmpty() {
        assertEquals(Collections.emptyList(), itemService.search(1L, "", Pageable.ofSize(5)));
    }
}