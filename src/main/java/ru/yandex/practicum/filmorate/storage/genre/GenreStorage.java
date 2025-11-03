package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenreStorage {

    List<Genre> findAll();

    Optional<Genre> findById(Long id);

    void validateGenresExist(Set<Genre> genres);


}
