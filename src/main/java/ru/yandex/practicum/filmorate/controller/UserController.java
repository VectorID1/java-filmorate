package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Get /users - получение всех пользователей: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User addUser(@RequestBody User user) {
        log.info("Post /users - добавление нового пользователя: email = {}", user.getEmail());

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
        user.setId(getNextIdUser());
        users.put(user.getId(), user);
        log.info("Пользователь добавлен: Id = {}", user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        log.info("Put /users - обновление пользователя: {}", newUser.getId());

        if (newUser.getId() == null || !users.containsKey(newUser.getId())) {
            log.warn("Id не указан или пользователя с таким Id нет: {}", newUser.getId());
            throw new ValidationException("Id не указан или пользователя с таким Id нет");
        }
        User oldUser = users.get(newUser.getId());

        if (newUser.getEmail() != null && newUser.getEmail().contains("@")) {
            log.info("email обновлен");
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getLogin() != null && !newUser.getLogin().contains(" ")) {
            log.info("login обновлен");
            oldUser.setLogin(newUser.getLogin());
        }
        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            log.info("имя пользователя обновленно");
            oldUser.setName(loginOfName(newUser));
        }
        if (newUser.getBirthday() != null && newUser.getBirthday().isBefore(LocalDate.now())) {
            log.info("дата рождения обновлена");
            oldUser.setBirthday(newUser.getBirthday());
        }
        users.put(oldUser.getId(), oldUser);

        return oldUser;
    }

    private String loginOfName(User user) {
        String newName = user.getName();
        if (user.getName() == null || user.getName().isBlank()) {
            newName = user.getLogin();
        }
        return newName;
    }

    private long getNextIdUser() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}