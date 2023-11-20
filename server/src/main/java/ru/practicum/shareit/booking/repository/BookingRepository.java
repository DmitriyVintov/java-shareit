package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusBooking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsBookingByIdAndItem_OwnerId(long bookingId, long ownerId);

    boolean existsBookingByIdAndStatusNot(long bookingId, StatusBooking statusBooking);

    boolean existsBookingByBookerIdAndItemIdAndStatusAndStartBefore(long userId, long itemId, StatusBooking statusBooking, LocalDateTime currentTime);

    List<Booking> findAllByBookerIdOrderByStartDesc(long userId, Pageable pageable);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(long userId, StatusBooking statusBooking, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(long userId, LocalDateTime end, Pageable pageable);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(long userId, LocalDateTime start, Pageable pageable);

    List<Booking> findAllByItem_OwnerIdOrderByStartDesc(long userId, Pageable pageable);

    List<Booking> findAllByItem_OwnerIdAndStatusOrderByStartDesc(long userId, StatusBooking statusBooking, Pageable pageable);

    List<Booking> findAllByItem_OwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findAllByItem_OwnerIdAndEndBeforeOrderByStartDesc(long userId, LocalDateTime end, Pageable pageable);

    List<Booking> findAllByItem_OwnerIdAndStartAfterOrderByStartDesc(long userId, LocalDateTime start, Pageable pageable);

    Optional<Booking> findFirstBookingByItemIdAndStatusNotAndStartBeforeOrderByStartDesc(long itemId, StatusBooking statusBooking, LocalDateTime currentTime);

    Optional<Booking> findFirstBookingByItemIdAndStatusNotAndStartAfterOrderByStart(long itemId, StatusBooking statusBooking, LocalDateTime currentTime);

    @Query(value = "select b from Booking b where b.item.id = ?1 and b.item.owner.id = ?2 and b.status <> ?3 and b.start < ?4 order by b.start desc")
    List<Booking> findLastBookingByOwnerId(long itemId, long bookerId, StatusBooking statusBooking, LocalDateTime currentTime);

    @Query(value = "select b from Booking b where b.item.id = ?1 and b.item.owner.id = ?2 and b.status <> ?3 and b.start > ?4 order by b.start")
    List<Booking> findNextBookingByOwnerId(long itemId, long bookerId, StatusBooking statusBooking, LocalDateTime currentTime);
}
