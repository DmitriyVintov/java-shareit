package ru.practicum.shareit.user;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DataAlreadyExistException;
import ru.practicum.shareit.exception.DataIntegrityViolationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    private final EasyRandom random = new EasyRandom();

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void getUsers() {
        List<User> users = random.objects(User.class, 3).collect(Collectors.toList());
        List<UserDto> usersDto = UserMapper.INSTANCE.toUsersDto(users);

        when(userRepository.findAll()).thenReturn(users);

        assertEquals(usersDto, userService.getUsers());
    }

    @Test
    @DisplayName("Получение пользователя по id")
    void getUserById() {
        User user = random.nextObject(User.class);
        UserDto userDto = UserMapper.INSTANCE.toUserDto(user);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(user));
        assertEquals(userDto, userService.getUserById(user.getId()));
    }

    @Test
    @DisplayName("Получение ошибки при получении пользователя по id, если пользователь не найден")
    void shouldThrowExceptionWhenGetUserByIdIfUserDoesNotExist() {
        when(userRepository.existsById(Mockito.anyLong())).thenReturn(false);
        assertThrows(NotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    @DisplayName("Создание пользователя")
    void addUser() {
        User user = random.nextObject(User.class);
        UserDto userDto = UserMapper.INSTANCE.toUserDto(user);

        when(userRepository.saveAndFlush(Mockito.any(User.class))).thenReturn(user);
        assertEquals(userDto, userService.addUser(userDto));
    }

    @Test
    @DisplayName("Получение ошибки, если пользователь с такими же именем и email существует")
    void shouldThrowExceptionWhenAddUserIfUserWithEmailAlreadyExist() {
        User user = random.nextObject(User.class);
        UserDto userDtoDuplicate = UserMapper.INSTANCE.toUserDto(user);

        when(userRepository.saveAndFlush(Mockito.any(User.class))).thenThrow(DataIntegrityViolationException.class);
        assertThrows(DataIntegrityViolationException.class, () -> userService.addUser(userDtoDuplicate));
    }

    @Test
    @DisplayName("Обновление пользователя")
    void updateUser() {
        User user = random.nextObject(User.class);
        UserDto userDto = UserMapper.INSTANCE.toUserDto(user);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(userRepository.existsByIdNotAndEmail(Mockito.anyLong(), Mockito.anyString())).thenReturn(false);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

        assertEquals(userDto, userService.updateUser(user.getId(), userDto));
    }

    @Test
    @DisplayName("Получение ошибки при обновлении пользователя, когда email не заполнен")
    void shouldThrowExceptionWhenUpdateUserIfEmailIsEmpty() {
        User user = random.nextObject(User.class);
        UserDto userDto = UserMapper.INSTANCE.toUserDto(user);
        userDto.setEmail(" ");

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(userRepository.existsByIdNotAndEmail(Mockito.anyLong(), Mockito.anyString())).thenReturn(false);

        assertThrows(ValidationException.class, () -> userService.updateUser(user.getId(), userDto));
    }

    @Test
    @DisplayName("Получение ошибки при обновлении пользователя, когда пользователь не найден")
    void shouldThrowExceptionWhenUpdateUserIfUserDoesNotExist() {
        User user = random.nextObject(User.class);
        UserDto userDto = UserMapper.INSTANCE.toUserDto(user);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.updateUser(user.getId(), userDto));
    }

    @Test
    @DisplayName("Получение ошибки при обновлении пользователя, когда пользователь уже существует")
    void shouldThrowExceptionWhenUpdateUserIfUserAlreadyExist() {
        User user = random.nextObject(User.class);
        UserDto userDto = UserMapper.INSTANCE.toUserDto(user);

        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        when(userRepository.existsByIdNotAndEmail(Mockito.anyLong(), Mockito.anyString())).thenReturn(true);

        assertThrows(DataAlreadyExistException.class, () -> userService.updateUser(user.getId(), userDto));
    }

    @Test
    @DisplayName("Удаление пользователя")
    void deleteUser() {
        long userId = 1L;
        when(userRepository.existsById(Mockito.anyLong())).thenReturn(true);
        Mockito.doNothing().when(userRepository).deleteById(Mockito.any());
        userService.deleteUser(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("Получение ошибки при удалении пользователя, если пользователь не найден")
    void shouldThrowExceptionWhenDeleteUserIfUserDoesNotExist() {
        when(userRepository.existsById(Mockito.anyLong())).thenReturn(false);
        assertThrows(NotFoundException.class, () -> userService.deleteUser(1L));
    }
}