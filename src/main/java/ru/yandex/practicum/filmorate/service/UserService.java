package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                new NotFoundException("Пользователя с id " + userId + " нет."));
    }

    public User addUser(User user) {
        validateUser(user);
        userStorage.save(user);
        log.info("Пользователь {} добавлен: id = {}", user.getName(), user.getId());
        return user;
    }


    public User updateUser(User user) {
        userStorage.findById(user.getId()).orElseThrow(() ->
                new NotFoundException("Пользователя с id " + user.getId() + " нет."));

        validateUser(user);
        User updateUser = userStorage.update(user);
        log.info("Пользователь {} обновлён.", updateUser.getName());
        return updateUser;
    }


    public void addFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getOutgoingRequests().add(friendId);
        friend.getIncomingRequests().add(userId);

        userStorage.update(user);
        userStorage.update(friend);
        log.info("Пользователи {} сделал запрос добавления в друзья пользователю {}.", user.getName(), friend.getName());
    }

    public void confirmFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (!friend.getOutgoingRequests().contains(userId)) {
            throw new NotFoundException("Нет запроса на дружбу от пользователя " + friend.getName());
        }

        user.getIncomingRequests().remove(friendId);
        friend.getOutgoingRequests().remove(userId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.update(user);
        userStorage.update(friend);
        log.info("Пользователи {} и {} теперь друзья", user.getName(), friend.getName());
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);
        log.info("Пользователи {} и {} больше не друзья!!!", user.getName(), friend.getName());
    }

    public List<User> getFriends(Long userId) {
        User user = getUserById(userId);
        return user.getFriends().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId1, Long userId2) {
        User user1 = getUserById(userId1);
        User user2 = getUserById(userId2);

        Set<Long> commonFriends = new HashSet<>(user1.getFriends());
        commonFriends.retainAll(user2.getFriends());
        log.info("Получение списка общих друзей пользователей: {} и {}", user1.getName(), user2.getName());

        return commonFriends.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Передан некорректный email: {}", user.getEmail());
            throw new ValidationException("Email не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Передан некорректный login: {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя задано автоматически: {}", user.getLogin());
            user.setName(loginOfName(user));
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Некоректная дата рождения: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private String loginOfName(User user) {
        String newName = user.getName();
        if (user.getName() == null || user.getName().isBlank()) {
            newName = user.getLogin();
        }
        return newName;
    }
}


