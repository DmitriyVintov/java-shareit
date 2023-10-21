package ru.practicum.shareit.user;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class UserServiceImplIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    private final EasyRandom random = new EasyRandom();

    @Test
    @DirtiesContext
    @DisplayName("Получение всех пользователей")
    void getUsers() {
        User user = random.nextObject(User.class);
        user.setId(null);
        userRepository.save(user);

        List<UserDto> users = userService.getUsers();

        assertEquals(1, users.size());
        assertEquals(user.getId(), users.get(0).getId());
        assertEquals(user.getName(), users.get(0).getName());
    }

    @Test
    @DirtiesContext
    @DisplayName("Создание пользователя")
    void addUser() {
        UserDto userDto = random.nextObject(UserDto.class);
        userDto.setId(null);

        UserDto user = userService.addUser(userDto);

        assertEquals(1, user.getId());
        assertEquals(userDto.getName(), user.getName());
        assertEquals(userDto.getEmail(), user.getEmail());
    }
}
