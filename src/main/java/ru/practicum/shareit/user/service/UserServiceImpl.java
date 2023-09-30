package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DataAlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
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
        return userRepository.findAll().stream()
                .map(UserMapper.INSTANCE::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(long userId) {
        return UserMapper.INSTANCE.toUserDto(userRepository.findById(userId).orElseThrow(() -> {
            String errorMessage = String.format("Пользователь id %s не найден", userId);
            log.error(errorMessage);
            return new NotFoundException(errorMessage);
        }));
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        return UserMapper.INSTANCE.toUserDto(userRepository.save(UserMapper.INSTANCE.toUser(userDto)));
    }

    @Override
    public UserDto updateUser(long userId, UserDto userDto) {
        checkUserByIdAndEmail(userId, userDto);
        User user = UserMapper.INSTANCE.toUser(getUserById(userId));
        user.setId(userId);
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.INSTANCE.toUserDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(long userId) {
        getUserById(userId);
        userRepository.deleteById(userId);
    }

    private void checkUserByIdAndEmail(long userId, UserDto userDto) {
        List<UserDto> collect = userRepository.findAllByIdNotAndEmail(userId, userDto.getEmail()).stream()
                .map(UserMapper.INSTANCE::toUserDto)
                .collect(Collectors.toList());
        if (!collect.isEmpty()) {
            String errorMessage = String.format("Пользователь id %s с email %s уже существует", userId, userDto.getEmail());
            log.error(errorMessage);
            throw new DataAlreadyExistException(errorMessage);
        }
    }
}