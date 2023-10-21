package ru.practicum.shareit.booking;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    private final EasyRandom random = new EasyRandom();

    @Test
    void existsBookingByIdAndItem_OwnerId() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        Booking booking = createBooking(item, booker);

        assertTrue(bookingRepository.existsBookingByIdAndItem_OwnerId(booking.getId(), owner.getId()));
    }

    @Test
    void existsBookingByIdAndStatusNot() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        Booking booking = createBooking(item, booker);

        assertTrue(bookingRepository.existsBookingByIdAndStatusNot(booking.getId(), StatusBooking.REJECTED));
    }

    @Test
    void existsBookingByBookerIdAndItemIdAndStatusAndStartBefore() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        Booking booking = createBooking(item, booker);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        booking.setStart(start);

        assertTrue(bookingRepository.existsBookingByBookerIdAndItemIdAndStatusAndStartBefore(
                booker.getId(), item.getId(), StatusBooking.WAITING, LocalDateTime.now()));
    }

    @Test
    void findAllByBookerIdOrderByStartDesc() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        createBooking(item, booker);
        Pageable pageable = Pageable.ofSize(20);

        assertEquals(1, bookingRepository.findAllByBookerIdOrderByStartDesc(booker.getId(), pageable).size());
    }

    @Test
    void findAllByBookerIdAndStatusOrderByStartDesc() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        createBooking(item, booker);
        Pageable pageable = Pageable.ofSize(20);

        assertEquals(1, bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(booker.getId(), StatusBooking.WAITING, pageable).size());
    }

    @Test
    void findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        Booking booking = createBooking(item, booker);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        booking.setStart(start);
        booking.setEnd(end);
        Pageable pageable = Pageable.ofSize(20);

        assertEquals(1, bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                booker.getId(), LocalDateTime.now(), LocalDateTime.now(), pageable).size());
    }

    @Test
    void findAllByBookerIdAndEndBeforeOrderByStartDesc() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        Booking booking = createBooking(item, booker);
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        booking.setStart(start);
        booking.setEnd(end);
        Pageable pageable = Pageable.ofSize(20);

        assertEquals(1, bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(
                booker.getId(), LocalDateTime.now(), pageable).size());
    }

    @Test
    void findAllByBookerIdAndStartAfterOrderByStartDesc() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        Booking booking = createBooking(item, booker);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        booking.setStart(start);
        booking.setEnd(end);
        Pageable pageable = Pageable.ofSize(20);

        assertEquals(1, bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(
                booker.getId(), LocalDateTime.now(), pageable).size());
    }

    @Test
    void findAllByItem_OwnerIdOrderByStartDesc() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        createBooking(item, booker);
        Pageable pageable = Pageable.ofSize(20);

        assertEquals(1, bookingRepository.findAllByItem_OwnerIdOrderByStartDesc(
                owner.getId(), pageable).size());
    }

    @Test
    void findAllByItem_OwnerIdAndStatusOrderByStartDesc() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        createBooking(item, booker);
        Pageable pageable = Pageable.ofSize(20);

        assertEquals(1, bookingRepository.findAllByItem_OwnerIdAndStatusOrderByStartDesc(
                owner.getId(), StatusBooking.WAITING, pageable).size());
    }

    @Test
    void findAllByItem_OwnerIdAndStartBeforeAndEndAfterOrderByStartDesc() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        Booking booking = createBooking(item, booker);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        booking.setStart(start);
        booking.setEnd(end);
        Pageable pageable = Pageable.ofSize(20);

        assertEquals(1, bookingRepository.findAllByItem_OwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                owner.getId(), LocalDateTime.now(), LocalDateTime.now(), pageable).size());
    }

    @Test
    void findAllByItem_OwnerIdAndEndBeforeOrderByStartDesc() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        Booking booking = createBooking(item, booker);
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        booking.setStart(start);
        booking.setEnd(end);
        Pageable pageable = Pageable.ofSize(20);

        assertEquals(1, bookingRepository.findAllByItem_OwnerIdAndEndBeforeOrderByStartDesc(
                owner.getId(), LocalDateTime.now(), pageable).size());
    }

    @Test
    void findAllByItem_OwnerIdAndStartAfterOrderByStartDesc() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        Booking booking = createBooking(item, booker);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        booking.setStart(start);
        booking.setEnd(end);
        Pageable pageable = Pageable.ofSize(20);

        assertEquals(1, bookingRepository.findAllByItem_OwnerIdAndStartAfterOrderByStartDesc(
                owner.getId(), LocalDateTime.now(), pageable).size());
    }

    @Test
    void findFirstBookingByItemIdAndStatusNotAndStartBeforeOrderByStartDesc() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        Booking booking = createBooking(item, booker);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        booking.setStart(start);
        booking.setEnd(end);

        assertEquals(booking, bookingRepository.findFirstBookingByItemIdAndStatusNotAndStartBeforeOrderByStartDesc(
                item.getId(), StatusBooking.REJECTED, LocalDateTime.now()).get());
    }

    @Test
    void findFirstBookingByItemIdAndStatusNotAndStartAfterOrderByStart() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        Booking booking = createBooking(item, booker);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        booking.setStart(start);
        booking.setEnd(end);

        assertEquals(booking, bookingRepository.findFirstBookingByItemIdAndStatusNotAndStartAfterOrderByStart(
                item.getId(), StatusBooking.REJECTED, LocalDateTime.now()).get());
    }

    @Test
    void findLastBookingByOwnerId() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        Booking booking = createBooking(item, booker);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        booking.setStart(start);
        booking.setEnd(end);

        assertEquals(1, bookingRepository.findLastBookingByOwnerId(
                item.getId(), owner.getId(), StatusBooking.REJECTED, LocalDateTime.now()).size());
    }

    @Test
    void findNextBookingByOwnerId() {
        User owner = createUser();
        User booker = createUser();
        Item item = createItem(owner);
        Booking booking = createBooking(item, booker);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        booking.setStart(start);
        booking.setEnd(end);

        assertEquals(1, bookingRepository.findNextBookingByOwnerId(
                item.getId(), owner.getId(), StatusBooking.REJECTED, LocalDateTime.now()).size());
    }

    private User createUser() {
        User owner = random.nextObject(User.class);
        return userRepository.save(owner);
    }

    private Booking createBooking(Item item, User booker) {
        Booking booking = random.nextObject(Booking.class);
        booking.setItem(item);
        booking.setBooker(booker);
        return bookingRepository.save(booking);
    }

    private Item createItem(User owner) {
        Item item = random.nextObject(Item.class);
        item.setOwner(owner);
        item.setRequest(null);
        return itemRepository.save(item);
    }
}