package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DateValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private User user;
    private UserController userController = new UserController();

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("testMail@yandex.ru");
        user.setName("TestName");
        user.setLogin("TestLogin");
        user.setBirthday(LocalDate.of(1991, 12, 15));
    }

    @Test
    void createUserValidData() {
        assertEquals(user.getName(), "TestName", "Имя не совпадает");
        assertEquals(user.getBirthday(), LocalDate.of(1991, 12, 15), "Дата рождения " +
                "не совпадает");
        assertEquals(user.getLogin(), "TestLogin", "Логин не совпадает");
        assertEquals(user.getEmail(), "testMail@yandex.ru", "Почта не совпадает");
    }

    @Test
    void emailNoValid() {
        assertDoesNotThrow(() -> userController.addUser(user));
        user.setEmail("");
        assertThrows(ConditionsNotMetException.class, () -> userController.addUser(user));
        user.setEmail(null);
        assertThrows(ConditionsNotMetException.class, () -> userController.addUser(user));
        user.setEmail("12312qwe.ry");
        assertThrows(ConditionsNotMetException.class, () -> userController.addUser(user));
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
        assertThrows(ConditionsNotMetException.class, () -> userController.addUser(user));
        user.setLogin(null);
        assertThrows(ConditionsNotMetException.class, () -> userController.addUser(user));
        user.setLogin("lo lo");
        assertThrows(ConditionsNotMetException.class, () -> userController.addUser(user));
    }

    @Test
    void birthdayNoValid() {
        assertDoesNotThrow(() -> userController.addUser(user));
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(DateValidationException.class, () -> userController.addUser(user));
    }
}