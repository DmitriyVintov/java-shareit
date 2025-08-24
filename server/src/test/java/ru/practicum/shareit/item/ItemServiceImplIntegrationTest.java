package ru.practicum.shareit.item;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@Disabled
public class ItemServiceImplIntegrationTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserRepository userRepository;
    private final EasyRandom random = new EasyRandom();

    @Test
    @DirtiesContext
    @DisplayName("Создание вещи")
    void addItem() {
        ItemDto itemDto = random.nextObject(ItemDto.class);
        itemDto.setId(null);
        User owner = userRepository.save(new User(
                itemDto.getOwner().getId(),
                itemDto.getOwner().getName(),
                "mail@yandex.ru"));
        itemDto.setRequestId(null);

        ItemDto itemDto1 = itemService.addItem(owner.getId(), itemDto);

        assertEquals(itemDto.getName(), itemDto1.getName());
        assertEquals(itemDto.getDescription(), itemDto1.getDescription());
    }
}
