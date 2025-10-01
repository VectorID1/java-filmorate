package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public FilmService(UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Collection<Film> getAllFilm() {
        log.info("Получение списка всех фильмов");
        return filmStorage.findAll();
    }

    public Film getFilmById(Long filmId) {
        return filmStorage.findById(filmId).orElseThrow(() -> {
            log.warn("Запрошен несуществующий фильм с ID: {}", filmId);
            return new NotFoundException(String.format("Фильм с id %s не найден", filmId));
        });
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        Film saveFilm = filmStorage.save(film);
        log.info("Фильм добавлен : {}", film.getName());
        return saveFilm;

    }

    public Film updateFilm(Film film) {
        filmStorage.findById(film.getId()).orElseThrow(() -> new NotFoundException("Фильм не найден"));
        validateFilm(film);
        Film updateFilm = filmStorage.update(film);

        log.info("Фильм {} обновлён.", updateFilm.getName());
        return updateFilm;
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм с Id " + filmId
                + " не найден."));
        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundException("User  с Id " + userId
                + " не найден."));
        if (film.getLikes().contains(userId)) {
            throw new ValidationException(String.format("Пользователь %s уже лайкал фильм : %s",
                    user.getName(), film.getName()));
        }
        film.getLikes().add(userId);
        filmStorage.update(film);
        log.info("User {}  поставил лайк фильму {}", user.getName(), film.getName());

    }

    public void removeLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм с Id " + filmId
                + " не найден."));
        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundException("User  с Id " + userId
                + "не найден."));

        if (!film.getLikes().contains(userId)) {
            throw new ValidationException(String.format("Пользователь %s не лайкал %s фильм!",
                    user.getName(), film.getName()));
        }
        film.getLikes().remove(userId);
        filmStorage.update(film);
        log.info("User {} удалил лайк фильма {}", user.getName(), film.getName());
    }

    public List<Film> getPopularFilms(Integer count) {

        if (count == null || count <= 0) {
            count = 10;
            log.debug("Установлено значение по умолчанию = {}", count);
        }
        log.info("Получение списка популярных фильмов");
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikeValue(), f1.getLikeValue()))
                .limit(count)
                .collect(Collectors.toList());

    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Передано пустое название фильма");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.warn("Превышение длинны описания: {} символов (Max 200)", film.getDescription().length());
            throw new ValidationException("максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Неправильная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза - не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.warn("Некорректная продолжительность фильма: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

}
