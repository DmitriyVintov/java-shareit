package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusBooking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsBookingByIdAndItem_OwnerId(long bookingId, long ownerId);

    boolean existsBookingByIdAndStatusNot(long bookingId, StatusBooking statusBooking);

    boolean existsBookingByBookerIdAndItemIdAndStatusAndStartBefore(long userId, long itemId, StatusBooking statusBooking, LocalDateTime currentTime);

    List<Booking> findAllByBookerIdOrderByStartDesc(long userId);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(long userId, StatusBooking statusBooking);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(long userId, LocalDateTime start, LocalDateTime end);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(long userId, LocalDateTime end);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(long userId, LocalDateTime start);

    List<Booking> findAllByItem_OwnerIdOrderByStartDesc(long userId);

    List<Booking> findAllByItem_OwnerIdAndStatusOrderByStartDesc(long userId, StatusBooking statusBooking);

    List<Booking> findAllByItem_OwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(long userId, LocalDateTime start, LocalDateTime end);

    List<Booking> findAllByItem_OwnerIdAndEndBeforeOrderByStartDesc(long userId, LocalDateTime end);

    List<Booking> findAllByItem_OwnerIdAndStartAfterOrderByStartDesc(long userId, LocalDateTime start);

    Optional<Booking> findFirstBookingByItemIdAndStatusNotAndStartBeforeOrderByStartDesc(long itemId, StatusBooking statusBooking, LocalDateTime currentTime);

    Optional<Booking> findFirstBookingByItemIdAndStatusNotAndStartAfterOrderByStart(long itemId, StatusBooking statusBooking, LocalDateTime currentTime);

    Optional<Booking> findFirstBookingByItemIdAndItem_OwnerIdAndStatusNotAndStartBeforeOrderByStartDesc(long itemId, long bookerId, StatusBooking statusBooking, LocalDateTime currentTime);

    Optional<Booking> findFirstBookingByItemIdAndItem_OwnerIdAndStatusNotAndStartAfterOrderByStart(long itemId, long bookerId, StatusBooking statusBooking, LocalDateTime currentTime);
}
