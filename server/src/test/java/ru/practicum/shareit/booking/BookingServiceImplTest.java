package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Disabled
class BookingServiceImplTest {
    private BookingService bookingService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    private final EasyRandom random = new EasyRandom();
    private final LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    private final LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, userRepository, itemRepository);
    }

    @Test
    @DisplayName("Создание бронирования")
    void addBooking() {
        Booking booking = random.nextObject(Booking.class);
        Item item = random.nextObject(Item.class);
        User booker = random.nextObject(User.class);
        BookingCreateDto bookingCreateDto = new BookingCreateDto(1L, yesterday, tomorrow, item.getId());

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(item));
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.save(Mockito.any())).thenReturn(booking);

        BookingFullDto bookingFullDto = BookingMapper.INSTANCE.toBookingFullDto(booking);
        BookingFullDto bookingFromDb = bookingService.addBooking(booker.getId(), bookingCreateDto);

        assertEquals(bookingFullDto, bookingFromDb);
    }

    @Test
    @DisplayName("Получение ошибки при создании бронирования, когда пользователь не найден")
    void shouldThrowExceptionWhenAddBookingIfUserDoesNotExist() {
        Item item = random.nextObject(Item.class);
        User booker = random.nextObject(User.class);
        BookingCreateDto bookingCreateDto = new BookingCreateDto(1L, yesterday, tomorrow, item.getId());
        bookingCreateDto.setItemId(item.getId());

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.addBooking(booker.getId(), bookingCreateDto));
    }

    @Test
    @DisplayName("Получение ошибки при создании бронирования, когда дата начала бронирования null")
    void shouldThrowExceptionWhenAddBookingIfBookingStartIsNull() {
        Item item = random.nextObject(Item.class);
        User booker = random.nextObject(User.class);
        BookingCreateDto bookingCreateDto = new BookingCreateDto(1L, null, tomorrow, item.getId());
        bookingCreateDto.setItemId(item.getId());

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);

        assertThrows(NotFoundException.class, () -> bookingService.addBooking(booker.getId(), bookingCreateDto));
    }

    @Test
    @DisplayName("Получение ошибки при создании бронирования, когда дата окончания бронирования null")
    void shouldThrowExceptionWhenAddBookingIfBookingEndIsNull() {
        Item item = random.nextObject(Item.class);
        User booker = random.nextObject(User.class);
        BookingCreateDto bookingCreateDto = new BookingCreateDto(1L, yesterday, null, item.getId());
        bookingCreateDto.setItemId(item.getId());

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);

        assertThrows(NotFoundException.class, () -> bookingService.addBooking(booker.getId(), bookingCreateDto));
    }

    @Test
    @DisplayName("Получение ошибки при создании бронирования, когда дата начала бронирования позже даты окончания бронирования")
    void shouldThrowExceptionWhenAddBookingIfBookingStartIsAfterBookingEnd() {
        Item item = random.nextObject(Item.class);
        User booker = random.nextObject(User.class);
        BookingCreateDto bookingCreateDto = new BookingCreateDto(1L, tomorrow, yesterday, item.getId());
        bookingCreateDto.setItemId(item.getId());

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);

        assertThrows(ValidationException.class, () -> bookingService.addBooking(booker.getId(), bookingCreateDto));
    }

    @Test
    @DisplayName("Получение ошибки при создании бронирования, когда даты начала и окончания бронирования совпадают")
    void shouldThrowExceptionWhenAddBookingIfBookingStartIsEqualBookingEnd() {
        Item item = random.nextObject(Item.class);
        User booker = random.nextObject(User.class);
        BookingCreateDto bookingCreateDto = new BookingCreateDto(1L, tomorrow, tomorrow, item.getId());
        bookingCreateDto.setItemId(item.getId());

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);

        assertThrows(ValidationException.class, () -> bookingService.addBooking(booker.getId(), bookingCreateDto));
    }

    @Test
    @DisplayName("Получение ошибки при создании бронирования, когда вещь не найдена")
    void shouldThrowExceptionWhenAddBookingIfItemDoesNotExist() {
        Item item = random.nextObject(Item.class);
        User booker = random.nextObject(User.class);
        BookingCreateDto bookingCreateDto = new BookingCreateDto(1L, yesterday, tomorrow, item.getId());
        bookingCreateDto.setItemId(item.getId());

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.existsById(Mockito.anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.addBooking(booker.getId(), bookingCreateDto));
    }

    @Test
    @DisplayName("Получение ошибки при создании бронирования, когда владелец вещи и бронирующий совпадают")
    void shouldThrowExceptionWhenAddBookingIfOwnerIsEqualBooker() {
        Item item = random.nextObject(Item.class);
        User booker = random.nextObject(User.class);
        BookingCreateDto bookingCreateDto = new BookingCreateDto(booker.getId(), yesterday, tomorrow, item.getId());
        bookingCreateDto.setItemId(item.getId());
        item.setOwner(booker);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.addBooking(booker.getId(), bookingCreateDto));
    }

    @Test
    @DisplayName("Получение ошибки при создании бронирования, когда вещь уже забронирована")
    void shouldThrowExceptionWhenAddBookingIfItemNotAvailable() {
        Item item = random.nextObject(Item.class);
        User booker = random.nextObject(User.class);
        BookingCreateDto bookingCreateDto = new BookingCreateDto(booker.getId(), yesterday, tomorrow, item.getId());
        bookingCreateDto.setItemId(item.getId());
        item.setAvailable(false);

        when(userRepository.existsById(booker.getId())).thenReturn(true);
        when(itemRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.addBooking(booker.getId(), bookingCreateDto));
    }

    @Test
    @DisplayName("Подтверждение или отклонение бронирования")
    void approvedOrRejectedBooking() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        Booking booking = random.nextObject(Booking.class);
        item.setOwner(owner);
        booking.setItem(item);
        booking.setBooker(booker);

        when(bookingRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.existsBookingByIdAndItem_OwnerId(Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.save(Mockito.any())).thenReturn(booking);

        booking.setStatus(StatusBooking.WAITING);
        BookingFullDto bookingFullDtoFromMethod = bookingService.approvedOrRejectedBooking(owner.getId(), booking.getId(), true);
        assertEquals(StatusBooking.APPROVED, bookingFullDtoFromMethod.getStatus());

        booking.setStatus(StatusBooking.WAITING);
        bookingFullDtoFromMethod = bookingService.approvedOrRejectedBooking(owner.getId(), booking.getId(), false);
        assertEquals(StatusBooking.REJECTED, bookingFullDtoFromMethod.getStatus());
    }

    @Test
    @DisplayName("Получение ошибки при подтверждении бронирования, когда бронирование не найдено")
    void shouldThrowExceptionWhenApprovedOrRejectedBookingIfBookingDoesNotExist() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        Booking booking = random.nextObject(Booking.class);
        item.setOwner(owner);
        booking.setItem(item);
        booking.setBooker(booker);

        when(bookingRepository.existsById(Mockito.anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.approvedOrRejectedBooking(owner.getId(), booking.getId(), true));
    }

    @Test
    @DisplayName("Получение ошибки при подтверждении бронирования, когда нет доступа для просмотра бронирования")
    void shouldThrowExceptionWhenApprovedOrRejectedBookingIfNoAccessToViewBooking() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        Booking booking = random.nextObject(Booking.class);
        item.setOwner(owner);
        booking.setItem(item);
        booking.setBooker(booker);

        when(bookingRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.approvedOrRejectedBooking(1L, booking.getId(), true));
    }

    @Test
    @DisplayName("Получение ошибки при подтверждении бронирования, когда изменение статуса бронирования невозможно")
    void shouldThrowExceptionWhenApprovedOrRejectedBookingIfStatusChangeIsNotPossible() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        Booking booking = random.nextObject(Booking.class);
        item.setOwner(owner);
        booking.setItem(item);
        booking.setBooker(booker);

        when(bookingRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.existsBookingByIdAndStatusNot(Mockito.anyLong(), Mockito.any(StatusBooking.class))).thenReturn(true);

        assertThrows(ValidationException.class, () -> bookingService.approvedOrRejectedBooking(owner.getId(), booking.getId(), true));
    }

    @Test
    @DisplayName("Получение ошибки при подтверждении бронирования, когда вещь не принадлежит подтверждающему пользователю")
    void shouldThrowExceptionWhenApprovedOrRejectedBookingIfItemDoesNotBelongConfirmingUser() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        Booking booking = random.nextObject(Booking.class);
        item.setOwner(owner);
        booking.setItem(item);
        booking.setBooker(booker);

        when(bookingRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.existsBookingByIdAndStatusNot(Mockito.anyLong(), Mockito.any(StatusBooking.class))).thenReturn(false);
        when(bookingRepository.existsBookingByIdAndItem_OwnerId(Mockito.anyLong(), Mockito.anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.approvedOrRejectedBooking(owner.getId(), booking.getId(), true));
    }

    @Test
    @DisplayName("Получение бронирования по id")
    void getBookingById() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        Booking booking = random.nextObject(Booking.class);
        item.setOwner(owner);
        booking.setItem(item);
        booking.setBooker(booker);

        when(bookingRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));

        BookingFullDto bookingFullDto = BookingMapper.INSTANCE.toBookingFullDto(booking);
        BookingFullDto bookingFromDb = bookingService.getBookingById(booker.getId(), booking.getId());

        assertEquals(bookingFullDto, bookingFromDb);
    }

    @Test
    @DisplayName("Получение ошибки при получении бронирования по id, когда бронирование не найдено")
    void shouldThrowExceptionWhenGetBookingByIdIfBookingDoesNotExist() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        Booking booking = random.nextObject(Booking.class);
        item.setOwner(owner);
        booking.setItem(item);
        booking.setBooker(booker);

        when(bookingRepository.existsById(Mockito.anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(booker.getId(), booking.getId()));
    }

    @Test
    @DisplayName("Получение ошибки при получении бронирования по id, когда нет доступа для просмотра бронирования")
    void shouldThrowExceptionWhenGetBookingByIdIfNoAccessToViewBooking() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        Booking booking = random.nextObject(Booking.class);
        item.setOwner(owner);
        booking.setItem(item);
        booking.setBooker(booker);

        when(bookingRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1L, booking.getId()));
    }

    @Test
    @DisplayName("Получение всех бронирований пользователя по id")
    void getBookingsByBookerId() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        item.setOwner(owner);
        List<Booking> bookings = random.objects(Booking.class, 5)
                .peek(booking -> {
                    booking.setBooker(booker);
                    booking.setItem(item);
                })
                .collect(Collectors.toList());
        List<BookingFullDto> bookingsFullDto = BookingMapper.INSTANCE.toBookingsFullDto(bookings);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(Mockito.anyLong(), Mockito.any(Pageable.class))).thenReturn(bookings);

        assertEquals(bookingsFullDto, bookingService.getBookingsByBookerId(booker.getId(), "ALL", Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение всех текущих бронирований пользователя по id")
    void getCurrentBookingsByBookerId() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        item.setOwner(owner);
        List<Booking> bookings = random.objects(Booking.class, 5)
                .peek(booking -> {
                    booking.setBooker(booker);
                    booking.setItem(item);
                })
                .collect(Collectors.toList());
        List<BookingFullDto> bookingsFullDto = BookingMapper.INSTANCE.toBookingsFullDto(bookings);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class))).thenReturn(bookings);

        assertEquals(bookingsFullDto, bookingService.getBookingsByBookerId(booker.getId(), "CURRENT", Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение всех прошедших бронирований пользователя по id")
    void getPastBookingsByBookerId() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        item.setOwner(owner);
        List<Booking> bookings = random.objects(Booking.class, 5)
                .peek(booking -> {
                    booking.setBooker(booker);
                    booking.setItem(item);
                })
                .collect(Collectors.toList());
        List<BookingFullDto> bookingsFullDto = BookingMapper.INSTANCE.toBookingsFullDto(bookings);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(
                Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class))).thenReturn(bookings);

        assertEquals(bookingsFullDto, bookingService.getBookingsByBookerId(booker.getId(), "PAST", Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение всех будущих бронирований пользователя по id")
    void getFutureBookingsByBookerId() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        item.setOwner(owner);
        List<Booking> bookings = random.objects(Booking.class, 5)
                .peek(booking -> {
                    booking.setBooker(booker);
                    booking.setItem(item);
                })
                .collect(Collectors.toList());
        List<BookingFullDto> bookingsFullDto = BookingMapper.INSTANCE.toBookingsFullDto(bookings);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(
                Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class))).thenReturn(bookings);

        assertEquals(bookingsFullDto, bookingService.getBookingsByBookerId(booker.getId(), "FUTURE", Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение всех бронирований в ожидании пользователя по id")
    void getBookingsWithStatusIsWaitingByBookerId() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        item.setOwner(owner);
        List<Booking> bookings = random.objects(Booking.class, 5)
                .peek(booking -> {
                    booking.setBooker(booker);
                    booking.setItem(item);
                })
                .collect(Collectors.toList());
        List<BookingFullDto> bookingsFullDto = BookingMapper.INSTANCE.toBookingsFullDto(bookings);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                Mockito.anyLong(), Mockito.any(StatusBooking.class), Mockito.any(Pageable.class))).thenReturn(bookings);

        assertEquals(bookingsFullDto, bookingService.getBookingsByBookerId(booker.getId(), "WAITING", Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение всех отклоненных бронирований пользователя по id")
    void getBookingsWithStatusIsRejectedByBookerId() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        item.setOwner(owner);
        List<Booking> bookings = random.objects(Booking.class, 5)
                .peek(booking -> {
                    booking.setBooker(booker);
                    booking.setItem(item);
                })
                .collect(Collectors.toList());
        List<BookingFullDto> bookingsFullDto = BookingMapper.INSTANCE.toBookingsFullDto(bookings);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                Mockito.anyLong(), Mockito.any(StatusBooking.class), Mockito.any(Pageable.class))).thenReturn(bookings);

        assertEquals(bookingsFullDto, bookingService.getBookingsByBookerId(booker.getId(), "REJECTED", Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение ошибки при получении всех бронирований пользователя по id со статусом UNSUPPORTED STATUS")
    void shouldThrowExceptionWhenGetBookingsWithIsUnsupportedStatusByBookerId() {
        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> bookingService.getBookingsByBookerId(1L, "UNSUPPORTED STATUS", Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение ошибки при получении всех бронирований пользователя по id, если пользователь не найден")
    void shouldThrowExceptionWhenGetBookingsByBookerIdIfUserDoesNotExist() {
        when(userRepository.existsById(Mockito.anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.getBookingsByBookerId(1L, "ALL", Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение всех бронирований владельца вещи по id")
    void getAllBookingsForItemsByOwnerId() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        item.setOwner(owner);
        List<Booking> bookings = random.objects(Booking.class, 5)
                .peek(booking -> {
                    booking.setBooker(booker);
                    booking.setItem(item);
                })
                .collect(Collectors.toList());
        List<BookingFullDto> bookingsFullDto = BookingMapper.INSTANCE.toBookingsFullDto(bookings);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findAllByItem_OwnerIdOrderByStartDesc(Mockito.anyLong(), Mockito.any(Pageable.class))).thenReturn(bookings);

        assertEquals(bookingsFullDto, bookingService.getAllBookingsForItemsByOwnerId(owner.getId(), "ALL", Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение всех текущих бронирований владельца вещи по id")
    void getAllCurrentBookingsForItemsByOwnerId() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        item.setOwner(owner);
        List<Booking> bookings = random.objects(Booking.class, 5)
                .peek(booking -> {
                    booking.setBooker(booker);
                    booking.setItem(item);
                })
                .collect(Collectors.toList());
        List<BookingFullDto> bookingsFullDto = BookingMapper.INSTANCE.toBookingsFullDto(bookings);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findAllByItem_OwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class))).thenReturn(bookings);

        assertEquals(bookingsFullDto, bookingService.getAllBookingsForItemsByOwnerId(owner.getId(), "CURRENT", Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение всех прошедших бронирований владельца вещи по id")
    void getAllPastBookingsForItemsByOwnerId() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        item.setOwner(owner);
        List<Booking> bookings = random.objects(Booking.class, 5)
                .peek(booking -> {
                    booking.setBooker(booker);
                    booking.setItem(item);
                })
                .collect(Collectors.toList());
        List<BookingFullDto> bookingsFullDto = BookingMapper.INSTANCE.toBookingsFullDto(bookings);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findAllByItem_OwnerIdAndEndBeforeOrderByStartDesc(
                Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class))).thenReturn(bookings);

        assertEquals(bookingsFullDto, bookingService.getAllBookingsForItemsByOwnerId(owner.getId(), "PAST", Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение всех будущих бронирований владельца вещи по id")
    void getAllFutureBookingsForItemsByOwnerId() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        item.setOwner(owner);
        List<Booking> bookings = random.objects(Booking.class, 5)
                .peek(booking -> {
                    booking.setBooker(booker);
                    booking.setItem(item);
                })
                .collect(Collectors.toList());
        List<BookingFullDto> bookingsFullDto = BookingMapper.INSTANCE.toBookingsFullDto(bookings);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findAllByItem_OwnerIdAndStartAfterOrderByStartDesc(
                Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class))).thenReturn(bookings);

        assertEquals(bookingsFullDto, bookingService.getAllBookingsForItemsByOwnerId(owner.getId(), "FUTURE", Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение всех бронирований в ожидании владельца вещи по id")
    void getAllBookingsForItemsWithStatusIsWaitingByOwnerId() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        item.setOwner(owner);
        List<Booking> bookings = random.objects(Booking.class, 5)
                .peek(booking -> {
                    booking.setBooker(booker);
                    booking.setItem(item);
                })
                .collect(Collectors.toList());
        List<BookingFullDto> bookingsFullDto = BookingMapper.INSTANCE.toBookingsFullDto(bookings);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findAllByItem_OwnerIdAndStatusOrderByStartDesc(
                Mockito.anyLong(), Mockito.any(StatusBooking.class), Mockito.any(Pageable.class))).thenReturn(bookings);

        assertEquals(bookingsFullDto, bookingService.getAllBookingsForItemsByOwnerId(owner.getId(), "WAITING", Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение всех отклоненных бронирований владельца вещи по id")
    void getAllBookingsForItemsWithStatusIsRejectedByOwnerId() {
        User owner = random.nextObject(User.class);
        User booker = random.nextObject(User.class);
        Item item = random.nextObject(Item.class);
        item.setOwner(owner);
        List<Booking> bookings = random.objects(Booking.class, 5)
                .peek(booking -> {
                    booking.setBooker(booker);
                    booking.setItem(item);
                })
                .collect(Collectors.toList());
        List<BookingFullDto> bookingsFullDto = BookingMapper.INSTANCE.toBookingsFullDto(bookings);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(bookingRepository.findAllByItem_OwnerIdAndStatusOrderByStartDesc(
                Mockito.anyLong(), Mockito.any(StatusBooking.class), Mockito.any(Pageable.class))).thenReturn(bookings);

        assertEquals(bookingsFullDto, bookingService.getAllBookingsForItemsByOwnerId(owner.getId(), "REJECTED", Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение ошибки при получении всех бронирований владельца вещи по id со статусом UNSUPPORTED STATUS")
    void shouldThrowExceptionWhenGetBookingsWithIsUnsupportedStatusByOwnerId() {
        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> bookingService.getAllBookingsForItemsByOwnerId(1L, "UNSUPPORTED STATUS", Pageable.ofSize(5)));
    }

    @Test
    @DisplayName("Получение ошибки при получении всех бронирований владельца вещи по id, если пользователь не найден")
    void shouldThrowExceptionWhenGetBookingsByOwnerIdIfUserDoesNotExist() {
        when(userRepository.existsById(Mockito.anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bookingService.getAllBookingsForItemsByOwnerId(1L, "ALL", Pageable.ofSize(5)));
    }
}