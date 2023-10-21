package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingFullDto addBooking(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @Valid @RequestBody BookingCreateDto bookingCreateDto) {
        return bookingService.addBooking(userId, bookingCreateDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingFullDto approvedOrRejectedBooking(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable long bookingId,
            @RequestParam boolean approved) {
        return bookingService.approvedOrRejectedBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingFullDto getBookingById(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingFullDto> getBookingsByBookerId(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(required = false, defaultValue = "ALL") String state,
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
            @RequestParam(required = false, defaultValue = "50") @Min(1) Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return bookingService.getBookingsByBookerId(userId, state, pageable);
    }

    @GetMapping("/owner")
    public List<BookingFullDto> getAllBookingsForItemsByOwnerId(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(required = false, defaultValue = "ALL") String state,
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
            @RequestParam(required = false, defaultValue = "50") @Min(1) Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return bookingService.getAllBookingsForItemsByOwnerId(userId, state, pageable);
    }
}