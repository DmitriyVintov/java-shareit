package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DataAlreadyExistException;
import ru.practicum.shareit.exception.DataIntegrityViolationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getUsers() {
        return UserMapper.INSTANCE.toUsersDto(userRepository.findAll());
    }

    @Override
    public UserDto getUserById(long userId) {
        checkUserById(userId);
        return UserMapper.INSTANCE.toUserDto(userRepository.findById(userId).get());
    }

    @Override
    @Transactional()
    public UserDto addUser(UserDto userDto) {
        try {
            return UserMapper.INSTANCE.toUserDto(userRepository.saveAndFlush(UserMapper.INSTANCE.toUser(userDto)));
        } catch (DataIntegrityViolationException e) {
            String errorMessage = String.format("Пользователь с %s и %s уже существует", userDto.getName(), userDto.getEmail());
            throw new DataIntegrityViolationException(errorMessage);
        }
    }

    @Override
    public UserDto updateUser(long userId, UserDto userDto) {
        checkUserById(userId);
        checkUserByIdAndEmail(userId, userDto);
        if ((userDto.getEmail() != null && userDto.getEmail().isBlank())
                || (userDto.getName() != null && userDto.getName().isBlank())) {
            String errorMessage = "Имя пользователя и email не могут быть пустыми";
            throw new ValidationException(errorMessage);
        }
        User user = userRepository.findById(userId).get();
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
        checkUserById(userId);
        userRepository.deleteById(userId);
    }

    private void checkUserById(long userId) {
        if (!userRepository.existsById(userId)) {
            String errorMessage = String.format("Пользователь id %s не найден", userId);
            throw new NotFoundException(errorMessage);
        }
    }

    private void checkUserByIdAndEmail(long userId, UserDto userDto) {
        if (userRepository.existsByIdNotAndEmail(userId, userDto.getEmail())) {
            String errorMessage = String.format("Пользователь id %s с email %s уже существует", userId, userDto.getEmail());
            throw new DataAlreadyExistException(errorMessage);
        }
    }
}