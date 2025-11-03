package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.request.FilmRequest;
import ru.yandex.practicum.filmorate.dto.response.FilmResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FilmRowMapper filmRowMapper;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public FilmService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("filmDbStorage") FilmStorage filmStorage,
                       FilmRowMapper filmRowMapper, MpaStorage mpaStorage, GenreStorage genreStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.filmRowMapper = filmRowMapper;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public List<FilmResponse> getAllFilms() {
        List<Film> films = filmStorage.findAll();
        return films.stream()
                .map(filmRowMapper::toFilmResponse)
                .collect(Collectors.toList());
    }

    public FilmResponse getFilmById(Long filmId) {
        Film film = filmStorage.findById(filmId).orElseThrow(() -> {
            log.warn("Запрошен несуществующий фильм с ID: {}", filmId);
            return new NotFoundException(String.format("Фильм с id %s не найден", filmId));
        });
        return filmRowMapper.toFilmResponse(film);
    }

    public FilmResponse addFilm(FilmRequest filmRequest) {
        Film film = filmRowMapper.toFilm(filmRequest);
        validateFilm(film);
        validateMpaExists(film.getMpa().getId());
        validateGenreExists(film.getGenres());
        Film savedFilm = filmStorage.save(film);
        log.info("Фильм добавлен: {}", film.getName());
        return filmRowMapper.toFilmResponse(savedFilm);
    }

    public FilmResponse updateFilm(FilmRequest filmRequest) {
        Film film = filmRowMapper.toFilm(filmRequest);
        validateFilm(film);
        validateMpaExists(film.getMpa().getId());
        validateGenreExists(film.getGenres());
        log.info("Валидация в FilmService прошла");
        Film updatedFilm = filmStorage.update(film);
        log.info("Фильм {} обновлён", updatedFilm.getName());
        return filmRowMapper.toFilmResponse(updatedFilm);
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с Id " + filmId + " не найден"));
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с Id " + userId + " не найден"));

        if (film.getLikes().contains(userId)) {
            throw new ValidationException(String.format("Пользователь %s уже лайкал фильм: %s",
                    user.getName(), film.getName()));
        }

        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", user.getName(), film.getName());
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с Id " + filmId + " не найден"));
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с Id " + userId + " не найден"));

        if (!film.getLikes().contains(userId)) {
            throw new ValidationException(String.format("Пользователь %s не лайкал фильм %s",
                    user.getName(), film.getName()));
        }

        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк фильма {}", user.getName(), film.getName());
    }

    public List<FilmResponse> getPopularFilms(Integer count) {
        if (count == null || count <= 0) {
            count = 10;
            log.debug("Установлено значение по умолчанию = {}", count);
        }
        log.info("Получение списка популярных фильмов");
                List<Film> films = filmStorage.findPopularFilms(count);
         return films.stream()
                 .map(filmRowMapper::toFilmResponse)
                 .collect(Collectors.toList());
    }
    private void validateMpaExists(Long mpaId) {
       if(!mpaStorage.existsMpaById(mpaId)) {
           throw new NotFoundException("Mpa с Id " + mpaId + " нет.");
        }
    }
    private void validateGenreExists(Set<Genre> genres) {
        genreStorage.validateGenresExist(genres);
    }


    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Передано пустое название фильма");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.warn("Превышение длины описания: {} символов (Max 200)", film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — 200 символов");
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
