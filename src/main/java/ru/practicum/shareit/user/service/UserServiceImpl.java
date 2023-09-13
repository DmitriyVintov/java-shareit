package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DataAlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getUsers() {
        return userRepository.getUsers().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(long userId) {
        checkUserById(userId);
        return UserMapper.toUserDto(userRepository.getUserById(userId));
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        checkUserByEmail(userDto);
        return UserMapper.toUserDto(userRepository.addUser(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto updateUser(long userId, UserDto userDto) {
        checkUserById(userId);
        checkUserByIdAndEmail(userId, userDto);
        User user = userRepository.getUserById(userId);
        user.setId(userId);
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(userRepository.updateUser(user));
    }

    @Override
    public void deleteUser(long userId) {
        checkUserById(userId);
        userRepository.deleteUser(userId);
    }

    @Override
    public void checkUserById(long userId) {
        userRepository.getUsers().stream()
                .filter(userDto -> userDto.getId() == userId)
                .findFirst()
                .orElseThrow(() -> {
                    log.info(String.format("Пользователь с id %s не найден", userId));
                    return new NotFoundException(String.format("Пользователь с id %s не найден", userId));
                });
    }

    @Override
    public void checkUserByEmail(UserDto userDto) {
        List<UserDto> collect = userRepository.getUsers().stream()
                .map(UserMapper::toUserDto)
                .filter(userDto1 -> userDto1.getEmail().equals(userDto.getEmail()))
                .collect(Collectors.toList());
        if (!collect.isEmpty()) {
            log.error(String.format("Пользователь с email %s уже существует", userDto.getEmail()));
            throw new DataAlreadyExistException(String.format("Пользователь с email %s уже существует", userDto.getEmail()));
        }
    }

    @Override
    public void checkUserByIdAndEmail(long userId, UserDto userDto) {
        List<UserDto> collect = userRepository.getUsers().stream()
                .map(UserMapper::toUserDto)
                .filter(userDto1 -> userDto1.getId() != userId)
                .filter(userDto1 -> userDto1.getEmail().equals(userDto.getEmail()))
                .collect(Collectors.toList());
        if (!collect.isEmpty()) {
            log.error(String.format("Пользователь с id %s и email %s уже существует", userId, userDto.getEmail()));
            throw new DataAlreadyExistException(String.format("Пользователь с id %s и email %s уже существует", userId, userDto.getEmail()));
        }
    }
}