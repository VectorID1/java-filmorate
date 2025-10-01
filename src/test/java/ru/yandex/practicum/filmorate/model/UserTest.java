package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private User user;
    private UserController userController;


    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        userController = new UserController(userService);
        user = new User();
        user.setEmail("testMail@yandex.ru");
        user.setName("TestName");
        user.setLogin("TestLogin");
        user.setBirthday(LocalDate.of(1991, 12, 15));
    }


    @Test
    void emailNoValid() {
        assertDoesNotThrow(() -> userController.addUser(user));
        user.setEmail("");
        assertThrows(ValidationException.class, () -> userController.addUser(user));
        user.setEmail(null);
        assertThrows(ValidationException.class, () -> userController.addUser(user));
        user.setEmail("12312qwe.ry");
        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void nameNoValid() {
        assertDoesNotThrow(() -> userController.addUser(user));
        user.setName("");
        userController.addUser(user);
        assertEquals(user.getName(), user.getLogin(), "Имя по умолчанию не установилось");
        user.setName(null);
        userController.addUser(user);
        assertEquals(user.getName(), user.getLogin(), "Имя по умолчанию не установилось");

    }

    @Test
    void loginNoValid() {
        assertDoesNotThrow(() -> userController.addUser(user));
        user.setLogin("");
        assertThrows(ValidationException.class, () -> userController.addUser(user));
        user.setLogin(null);
        assertThrows(ValidationException.class, () -> userController.addUser(user));
        user.setLogin("lo lo");
        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void birthdayNoValid() {
        assertDoesNotThrow(() -> userController.addUser(user));
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }
}