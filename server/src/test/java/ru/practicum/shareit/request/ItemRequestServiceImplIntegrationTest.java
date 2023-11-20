package ru.practicum.shareit.request;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@Disabled
public class ItemRequestServiceImplIntegrationTest {
    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private UserRepository userRepository;
    private final EasyRandom random = new EasyRandom();

    @Test
    @DirtiesContext
    @DisplayName("Создание запроса вещи")
    void addItemRequest() {
        ItemRequestCreateDto itemRequestCreateDto = random.nextObject(ItemRequestCreateDto.class);
        User owner = userRepository.save(new User(1L, "name", "mail@yandex.ru"));

        ItemRequestFullDto itemRequestFullDto = itemRequestService.addItemRequest(owner.getId(), itemRequestCreateDto);

        assertEquals(1, itemRequestFullDto.getId());
        assertEquals(itemRequestCreateDto.getDescription(), itemRequestFullDto.getDescription());
    }
}
