package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> findAll() {
        log.info("Get /users - получение всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        log.info("Get /users/{} - получение пользователя", id);
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getAllFriends(@PathVariable Long id) {
        log.info("Get /users/{}/friends - получение списка друзей", id);
        return userService.getFriends(id);
    }

    @PostMapping
    public User addUser(@RequestBody User user) {
        log.info("Post /users - добавление нового пользователя: email = {}", user.getEmail());
        return userService.addUser(user);
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        log.info("Put /users - обновление пользователя: {}", newUser.getId());
        return userService.updateUser(newUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addNewFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Put /users/{}/friends/{} - добавление в друзья", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Delete /users/{}/friends/{} - удаление из друзей", id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Get /users/{}/friends/common/{} - получение списка общих друзей", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}