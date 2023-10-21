package ru.practicum.shareit.booking;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class BookingServiceImplIntegrationTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    private final EasyRandom random = new EasyRandom();

    @Test
    @DirtiesContext
    @DisplayName("Создание бронирования")
    void addBooking() {
        BookingCreateDto bookingCreateDto = random.nextObject(BookingCreateDto.class);
        bookingCreateDto.setId(null);
        bookingCreateDto.setStart(LocalDateTime.now().plusDays(1));
        bookingCreateDto.setEnd(LocalDateTime.now().plusDays(2));
        User owner = userRepository.save(random.nextObject(User.class));
        User booker = userRepository.save(random.nextObject(User.class));
        Item item = random.nextObject(Item.class);
        item.setOwner(owner);
        item.setRequest(null);
        item.setAvailable(true);
        Item itemSave = itemRepository.save(item);
        bookingCreateDto.setItemId(itemSave.getId());

        BookingFullDto bookingFullDto = bookingService.addBooking(booker.getId(), bookingCreateDto);

        assertEquals(1, bookingFullDto.getId());
        assertEquals(bookingCreateDto.getItemId(), bookingFullDto.getItem().getId());
        assertEquals(bookingCreateDto.getStart(), bookingFullDto.getStart());
        assertEquals(bookingCreateDto.getEnd(), bookingFullDto.getEnd());
    }
}
