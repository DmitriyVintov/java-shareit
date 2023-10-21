package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    @MockBean
    private BookingService bookingService;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    private final EasyRandom random = new EasyRandom();
    private final String HEADER_USER_ID = "X-Sharer-User-Id";

    @Test
    @DisplayName("Создание бронирования")
    void addBooking() throws Exception {
        long userId = 1L;
        BookingCreateDto bookingCreateDto = random.nextObject(BookingCreateDto.class);
        bookingCreateDto.setStart(LocalDateTime.now().plusDays(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(2));
        Booking booking = BookingMapper.INSTANCE.toBookingFromBookingCreateDto(bookingCreateDto);
        BookingFullDto bookingFullDto = BookingMapper.INSTANCE.toBookingFullDto(booking);

        when(bookingService.addBooking(Mockito.anyLong(), Mockito.any(BookingCreateDto.class))).thenReturn(bookingFullDto);

        mvc.perform(post("/bookings")
                        .header(HEADER_USER_ID, userId)
                        .content(mapper.writeValueAsString(bookingCreateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingCreateDto.getId()));

        Mockito.verify(bookingService).addBooking(Mockito.anyLong(), Mockito.any(BookingCreateDto.class));
    }

    @Test
    @DisplayName("Подтверждение или отклонение бронирования")
    void approvedOrRejectedBooking() throws Exception {
        long userId = 1L;
        BookingCreateDto bookingCreateDto = random.nextObject(BookingCreateDto.class);
        bookingCreateDto.setStart(LocalDateTime.now().plusDays(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(2));
        Booking booking = BookingMapper.INSTANCE.toBookingFromBookingCreateDto(bookingCreateDto);
        BookingFullDto bookingFullDto = BookingMapper.INSTANCE.toBookingFullDto(booking);

        when(bookingService.approvedOrRejectedBooking(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(bookingFullDto);

        mvc.perform(patch("/bookings/{bookingId}", booking.getId())
                        .header(HEADER_USER_ID, userId)
                        .content(mapper.writeValueAsString(bookingCreateDto))
                        .param("approved", String.valueOf(true))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingCreateDto.getId()));

        Mockito.verify(bookingService).approvedOrRejectedBooking(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean());
    }

    @Test
    @DisplayName("Получение бронирования по id")
    void getBookingById() throws Exception {
        long userId = 1L;
        BookingCreateDto bookingCreateDto = random.nextObject(BookingCreateDto.class);
        bookingCreateDto.setStart(LocalDateTime.now().plusDays(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(2));
        Booking booking = BookingMapper.INSTANCE.toBookingFromBookingCreateDto(bookingCreateDto);
        BookingFullDto bookingFullDto = BookingMapper.INSTANCE.toBookingFullDto(booking);

        when(bookingService.getBookingById(Mockito.anyLong(), Mockito.anyLong())).thenReturn(bookingFullDto);

        mvc.perform(get("/bookings/{bookingId}", booking.getId())
                        .header(HEADER_USER_ID, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingCreateDto.getId()));

        Mockito.verify(bookingService).getBookingById(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("Получение списка всех бронирований текущего пользователя")
    void getBookingsByBookerId() throws Exception {
        long userId = 1L;
        BookingCreateDto bookingCreateDto = random.nextObject(BookingCreateDto.class);
        bookingCreateDto.setStart(LocalDateTime.now().plusDays(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(2));
        Booking booking = BookingMapper.INSTANCE.toBookingFromBookingCreateDto(bookingCreateDto);
        BookingFullDto bookingFullDto = BookingMapper.INSTANCE.toBookingFullDto(booking);
        List<BookingFullDto> bookingFullDtoList = List.of(bookingFullDto);

        when(bookingService.getBookingsByBookerId(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Pageable.class))).thenReturn(bookingFullDtoList);

        mvc.perform(get("/bookings")
                        .header(HEADER_USER_ID, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingFullDtoList.get(0).getId()));

        Mockito.verify(bookingService).getBookingsByBookerId(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Pageable.class));
    }

    @Test
    @DisplayName("Получение списка бронирований для всех вещей текущего пользователя")
    void getAllBookingsForItemsByOwnerId() throws Exception {
        long userId = 1L;
        BookingCreateDto bookingCreateDto = random.nextObject(BookingCreateDto.class);
        bookingCreateDto.setStart(LocalDateTime.now().plusDays(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(2));
        Booking booking = BookingMapper.INSTANCE.toBookingFromBookingCreateDto(bookingCreateDto);
        BookingFullDto bookingFullDto = BookingMapper.INSTANCE.toBookingFullDto(booking);
        List<BookingFullDto> bookingFullDtoList = List.of(bookingFullDto);

        when(bookingService.getAllBookingsForItemsByOwnerId(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Pageable.class))).thenReturn(bookingFullDtoList);

        mvc.perform(get("/bookings/owner")
                        .header(HEADER_USER_ID, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingFullDtoList.get(0).getId()));

        Mockito.verify(bookingService).getAllBookingsForItemsByOwnerId(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Pageable.class));
    }
}