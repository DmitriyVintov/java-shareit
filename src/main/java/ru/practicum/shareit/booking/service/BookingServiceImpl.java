package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemService itemService;

    @Override
    public BookingFullDto addBooking(long bookerId, BookingCreateDto bookingCreateDto) {
        checkExistUser(bookerId);
        checkDateEndIsAfterStart(bookingCreateDto);
        Long itemId = bookingCreateDto.getItemId();
        Item item = ItemMapper.INSTANCE.toItem(itemService.getItemById(itemId, bookerId));
        Long ownerId = item.getOwner().getId();
        checkMatchBookerAndOwner(bookerId, ownerId);
        checkAvailabilityItem(item);
        Booking booking = BookingMapper.INSTANCE.toBookingFromBookingCreateDto(bookingCreateDto);
        booking.setItem(item);
        booking.setBooker(userRepository.findById(bookerId).get());
        booking.setStatus(StatusBooking.WAITING);
        return BookingMapper.INSTANCE.toBookingFullDto(bookingRepository.save(booking));
    }

    @Override
    public BookingFullDto approvedOrRejectedBooking(long userId, long bookingId, boolean approved) {
        checkExistBooking(bookingId);
        Booking booking = bookingRepository.findById(bookingId).get();
        checkAccessForViewing(userId, booking.getBooker().getId(), booking.getItem().getOwner().getId());
        checkPossibilityChangeStatus(bookingId, booking);
        checkItemInBookingOwnedUser(bookingId, userId);
        booking.setStatus(approved ? StatusBooking.APPROVED : StatusBooking.REJECTED);
        return BookingMapper.INSTANCE.toBookingFullDto(bookingRepository.save(booking));
    }

    @Override
    public BookingFullDto getBookingById(long userId, long bookingId) {
        checkExistBooking(bookingId);
        Booking booking = bookingRepository.findById(bookingId).get();
        long bookerId = booking.getBooker().getId();
        long ownerId = booking.getItem().getOwner().getId();
        checkAccessForViewing(userId, bookerId, ownerId);
        return BookingMapper.INSTANCE.toBookingFullDto(booking);
    }

    @Override
    public List<BookingFullDto> getBookingsByBookerId(long bookerId, String state, Pageable pageable) {
        checkExistUser(bookerId);
        StateBooking stateBooking = StateBooking.valueOf(state);
        switch (stateBooking) {
            case ALL:
                return BookingMapper.INSTANCE.toBookingsFullDto(
                        bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId, pageable));
            case CURRENT:
                return BookingMapper.INSTANCE.toBookingsFullDto(bookingRepository
                        .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                bookerId, LocalDateTime.now(), LocalDateTime.now(), pageable));
            case PAST:
                return BookingMapper.INSTANCE.toBookingsFullDto(bookingRepository
                        .findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerId, LocalDateTime.now(), pageable));
            case FUTURE:
                return BookingMapper.INSTANCE.toBookingsFullDto(bookingRepository
                        .findAllByBookerIdAndStartAfterOrderByStartDesc(bookerId, LocalDateTime.now(), pageable));
            case WAITING:
                return BookingMapper.INSTANCE.toBookingsFullDto(
                        bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId, StatusBooking.WAITING, pageable));
            case REJECTED:
                return BookingMapper.INSTANCE.toBookingsFullDto(
                        bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId, StatusBooking.REJECTED, pageable));
            default:
                throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    public List<BookingFullDto> getAllBookingsForItemsByOwnerId(long ownerId, String state, Pageable pageable) {
        checkExistUser(ownerId);
        StateBooking stateBooking = StateBooking.valueOf(state);
        switch (stateBooking) {
            case ALL:
                return BookingMapper.INSTANCE.toBookingsFullDto(
                        bookingRepository.findAllByItem_OwnerIdOrderByStartDesc(ownerId, pageable));
            case CURRENT:
                return BookingMapper.INSTANCE.toBookingsFullDto(
                        bookingRepository.findAllByItem_OwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                ownerId, LocalDateTime.now(), LocalDateTime.now(), pageable));
            case PAST:
                return BookingMapper.INSTANCE.toBookingsFullDto(
                        bookingRepository.findAllByItem_OwnerIdAndEndBeforeOrderByStartDesc(ownerId, LocalDateTime.now(), pageable));
            case FUTURE:
                return BookingMapper.INSTANCE.toBookingsFullDto(
                        bookingRepository.findAllByItem_OwnerIdAndStartAfterOrderByStartDesc(ownerId, LocalDateTime.now(), pageable));
            case WAITING:
                return BookingMapper.INSTANCE.toBookingsFullDto(
                        bookingRepository.findAllByItem_OwnerIdAndStatusOrderByStartDesc(ownerId, StatusBooking.WAITING, pageable));
            case REJECTED:
                return BookingMapper.INSTANCE.toBookingsFullDto(
                        bookingRepository.findAllByItem_OwnerIdAndStatusOrderByStartDesc(ownerId, StatusBooking.REJECTED, pageable));
            default:
                throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    private void checkExistUser(long userId) {
        if (!userRepository.existsById(userId)) {
            String errorMessage = String.format("Пользователь id %s не найден", userId);
            throw new NotFoundException(errorMessage);
        }
    }

    private void checkAvailabilityItem(Item item) {
        if (!item.getAvailable()) {
            String errorMessage = String.format("Вещь id %s уже забронирована", item.getId());
            throw new ValidationException(errorMessage);
        }
    }

    private void checkItemInBookingOwnedUser(long bookingId, long userId) {
        if (!bookingRepository.existsBookingByIdAndItem_OwnerId(bookingId, userId)) {
            String errorMessage = String.format("Пользователь id %s не является владельцем вещи", userId);
            throw new NotFoundException(errorMessage);
        }
    }

    private void checkDateEndIsAfterStart(BookingCreateDto bookingCreateDto) {
        LocalDateTime start = bookingCreateDto.getStart();
        LocalDateTime end = bookingCreateDto.getEnd();
        if (start == null || end == null) {
            String errorMessage = "Даты начала и окончания бронирования не могут быть пустыми";
            throw new NotFoundException(errorMessage);
        }
        if (start.isAfter(end)) {
            String errorMessage = "Дата начала бронирования не может быть позже даты окончания бронирования";
            throw new ValidationException(errorMessage);
        }
        if (start.equals(end)) {
            String errorMessage = "Даты начала и окончания бронирования не могут совпадать";
            throw new ValidationException(errorMessage);
        }
    }

    private void checkAccessForViewing(long userId, long bookerId, long ownerId) {
        if (userId != bookerId && userId != ownerId) {
            String errorMessage = String.format("Пользователь id %s не может просматривать данное бронирование", userId);
            throw new NotFoundException(errorMessage);
        }
    }

    private void checkExistBooking(long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            String errorMessage = String.format("Бронирование id %s не найдено", bookingId);
            throw new NotFoundException(errorMessage);
        }
    }

    private void checkPossibilityChangeStatus(long bookingId, Booking booking) {
        if (bookingRepository.existsBookingByIdAndStatusNot(bookingId, StatusBooking.WAITING)) {
            StatusBooking status = booking.getStatus();
            String errorMessage = String.format("Изменение статуса бронирования запрещено, потому что у него статус \"%s\"", status);
            throw new ValidationException(errorMessage);
        }
    }

    private void checkMatchBookerAndOwner(long bookerId, Long ownerId) {
        if (ownerId == bookerId) {
            String errorMessage = "Бронирование невозможно. Владелец вещи и пользователь совпадают";
            throw new NotFoundException(errorMessage);
        }
    }
}