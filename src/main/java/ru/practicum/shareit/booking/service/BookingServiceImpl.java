package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StateBooking;
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public BookingFullDto addBooking(long bookerId, BookingCreateDto bookingCreateDto) {
        checkExistUser(bookerId);
        Long itemId = bookingCreateDto.getItemId();
        Item item = ItemMapper.INSTANCE.toItem(itemService.getItemById(itemId, bookerId));
        Long ownerId = item.getOwner().getId();
        if (ownerId == bookerId) {
            String errorMessage = "Бронирование не возможно. Владелец вещи и пользователь совпадают";
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        checkAvailabilityItem(item);
        Booking booking = BookingMapper.INSTANCE.toBookingFromBookingCreateDto(bookingCreateDto);
        booking.setItem(item);
        booking.setBooker(UserMapper.INSTANCE.toUser(userService.getUserById(bookerId)));
        booking.setStatus(StatusBooking.WAITING);
        return BookingMapper.INSTANCE.toBookingFullDto(bookingRepository.save(booking));
    }

    @Override
    public BookingFullDto approvedOrRejectedBooking(long ownerId, long bookingId, boolean approved) {
        checkItemInBookingOwnedUser(bookingId, ownerId);
        if (bookingRepository.existsBookingByIdAndStatusNot(bookingId, StatusBooking.WAITING)) {
            StatusBooking status = bookingRepository.findById(bookingId).get().getStatus();
            String errorMessage = String.format("Изменение статуса бронирования запрещено, потому что у него статус \"%s\"", status);
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
        Booking booking = BookingMapper.INSTANCE.toBookingFromBookingFullDto(getBookingById(ownerId, bookingId));
        if (approved) {
            booking.setStatus(StatusBooking.APPROVED);
        } else {
            booking.setStatus(StatusBooking.REJECTED);
        }
        return BookingMapper.INSTANCE.toBookingFullDto(bookingRepository.save(booking));
    }

    @Override
    public BookingFullDto getBookingById(long userId, long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            String errorMessage = String.format("Бронирование id %s не найдено", bookingId);
            log.error(errorMessage);
            return new NotFoundException(errorMessage);
        });
        long bookerId = booking.getBooker().getId();
        long ownerId = booking.getItem().getOwner().getId();
        if (userId != bookerId && userId != ownerId) {
            String errorMessage = String.format("Пользователь id %s не может просматривать данное бронирование", userId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return BookingMapper.INSTANCE.toBookingFullDto(booking);
    }

    @Override
    public List<BookingFullDto> getBookingsByBookerId(long bookerId, String state) {
        checkExistUser(bookerId);
        StateBooking stateBooking = StateBooking.valueOf(state);
        switch (stateBooking) {
            case ALL:
                return bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId).stream()
                        .map(BookingMapper.INSTANCE::toBookingFullDto).collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        bookerId, LocalDateTime.now(), LocalDateTime.now()
                ).stream().map(BookingMapper.INSTANCE::toBookingFullDto).collect(Collectors.toList());
            case PAST:
                return bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerId, LocalDateTime.now()).stream()
                        .map(BookingMapper.INSTANCE::toBookingFullDto).collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(bookerId, LocalDateTime.now()).stream()
                        .map(BookingMapper.INSTANCE::toBookingFullDto).collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId, StatusBooking.WAITING).stream()
                        .map(BookingMapper.INSTANCE::toBookingFullDto).collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId, StatusBooking.REJECTED).stream()
                        .map(BookingMapper.INSTANCE::toBookingFullDto).collect(Collectors.toList());
            case UNSUPPORTED_STATUS:
                throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
            default:
                throw new ValidationException("Неизвестный статус бронирования");
        }
    }

    @Override
    public List<BookingFullDto> getAllBookingsForItemsByOwnerId(long ownerId, String state) {
        checkExistUser(ownerId);
        StateBooking stateBooking = StateBooking.valueOf(state);
        switch (stateBooking) {
            case ALL:
                return bookingRepository.findAllByItem_OwnerIdOrderByStartDesc(ownerId).stream()
                        .map(BookingMapper.INSTANCE::toBookingFullDto).collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllByItem_OwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        ownerId, LocalDateTime.now(), LocalDateTime.now()
                ).stream().map(BookingMapper.INSTANCE::toBookingFullDto).collect(Collectors.toList());
            case PAST:
                return bookingRepository.findAllByItem_OwnerIdAndEndBeforeOrderByStartDesc(ownerId, LocalDateTime.now()).stream()
                        .map(BookingMapper.INSTANCE::toBookingFullDto).collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findAllByItem_OwnerIdAndStartAfterOrderByStartDesc(ownerId, LocalDateTime.now()).stream()
                        .map(BookingMapper.INSTANCE::toBookingFullDto).collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findAllByItem_OwnerIdAndStatusOrderByStartDesc(ownerId, StatusBooking.WAITING).stream()
                        .map(BookingMapper.INSTANCE::toBookingFullDto).collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findAllByItem_OwnerIdAndStatusOrderByStartDesc(ownerId, StatusBooking.REJECTED).stream()
                        .map(BookingMapper.INSTANCE::toBookingFullDto).collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    private void checkExistUser(long userId) {
        if (!userRepository.existsById(userId)) {
            String errorMessage = String.format("Пользователь id %s не найден", userId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    private void checkAvailabilityItem(Item item) {
        if (!item.getAvailable()) {
            String errorMessage = String.format("Вещь id %s уже забронирована", item.getId());
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }
    }

    private void checkItemInBookingOwnedUser(long bookingId, long userId) {
        if (!bookingRepository.existsBookingByIdAndItem_OwnerId(bookingId, userId)) {
            String errorMessage = String.format("Пользователь id %s не является владельцем вещи", userId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }
}