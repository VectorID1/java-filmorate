package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DateValidationException;
import ru.yandex.practicum.filmorate.exception.InvalidDurationException;
import ru.yandex.practicum.filmorate.exception.LengthExceededException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
@Service
@RestController()
@RequestMapping("/films")
public class FilmController {

    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Get /fimls - получение всех пользователей: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("Post /films - добавление нового фильма: name = {}", film.getName());

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Передано пустое название фильма");
            throw new ConditionsNotMetException("Название не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.warn("Превышение длинны описания: {} символов (Max 200)", film.getDescription().length());
            throw new LengthExceededException("максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Неправильная дата релиза: {}", film.getReleaseDate());
            throw new DateValidationException("Дата релиза - не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.warn("Некорректная продолжительность фильма: {}", film.getDuration());
            throw new InvalidDurationException("Продолжительность фильма должна быть положительным числом");
        }
        film.setId(getNextIdFilm());
        films.put(film.getId(), film);
        log.info("Фильм добавлен : {}", film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        log.info("Обновление фильма: {}", newFilm.getName());

        if (newFilm.getId() == null || !films.containsKey(newFilm.getId())) {
            log.warn("Id не указан или фильма с таким Id нет: {}", newFilm.getId());
            throw new ConditionsNotMetException("Id не указан или такого Id нет");
        }

        Film oldFilm = films.get(newFilm.getId());

        if (newFilm.getName() != null) {
            log.info("Новое название фильма: {}", newFilm.getName());
            oldFilm.setName(newFilm.getName());
        }
        if (newFilm.getReleaseDate() != null &&
                !newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.info("Новая дата релиза фильма: {}", newFilm.getReleaseDate());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null && newFilm.getDuration() > 0) {
            log.info("Новая продолжительность фильма: {}", newFilm.getDuration());
            oldFilm.setDuration(newFilm.getDuration());
        }
        if (newFilm.getDescription() != null && newFilm.getDescription().length() < 200) {
            log.info("Новое описание фильма: {}", newFilm.getDescription());
            oldFilm.setDescription(newFilm.getDescription());
        }
        films.put(oldFilm.getId(), oldFilm);
        log.info("фильм {} обновлен!", newFilm.getName());
        return oldFilm;

    }


    private long getNextIdFilm() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}