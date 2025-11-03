package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        log.info("Получение списка всех пользователей");
        return userStorage.findAll();
    }

    public User getUserById(Long userId) {
        log.info("Получение пользователя с id {}", userId);
        return userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    public User addUser(User user) {
        validateUser(user);
        User savedUser = userStorage.save(user);
        log.info("Пользователь {} добавлен: id = {}", savedUser.getName(), savedUser.getId());
        return savedUser;
    }

    public User updateUser(User user) {
        userStorage.findById(user.getId()).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + user.getId() + " не найден"));

        validateUser(user);
        User updatedUser = userStorage.update(user);
        log.info("Пользователь {} обновлён", updatedUser.getName());
        return updatedUser;
    }

    public void addFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);
        userStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        User user = getUserById(userId);
        log.info("Получение списка друзей пользователя {}",user.getName());
        return userStorage.findAllByIds(new ArrayList<>(user.getFriends()));
    }

    public List<User> getCommonFriends(Long userId1, Long userId2) {
        Set<Long> commonFriends = new HashSet<>(getUserById(userId1).getFriends());
        commonFriends.retainAll(getUserById(userId2).getFriends());

        log.info("Получение списка общих друзей пользователей: {} и {}", userId1,userId2);

        if (commonFriends.isEmpty()) {
            return List.of();
        }
        return userStorage.findAllByIds(new ArrayList<>(commonFriends));
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Передан некорректный email: {}", user.getEmail());
            throw new ValidationException("Email не может быть пустым и должен содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Передан некорректный login: {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя задано автоматически: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Некорректная дата рождения: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}

