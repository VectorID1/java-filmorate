package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User save(User user);
    User update(User user);
    void delete(Long userId);
    Optional<User> findById(Long id);
    List<User> findAll();

}
