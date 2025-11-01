package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.request.FilmRequest;
import ru.yandex.practicum.filmorate.dto.response.FilmResponse;
import ru.yandex.practicum.filmorate.mapper.FilmMappers;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;
    private final FilmMappers filmMapper;

    public FilmController(FilmService filmService, FilmMappers filmMapper) {
        this.filmService = filmService;
        this.filmMapper = filmMapper;
    }

    @GetMapping
    public List<FilmResponse> findAll() {
        log.info("Get /films - получение всех фильмов");
        List<Film> films = filmService.getAllFilm();

        return films.stream()
                .map(filmMapper::toFilmResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public FilmResponse getFilmById(@PathVariable Long id) {
        Film film = filmService.getFilmById(id);
        log.info("Get /films/{} - получение фильма", id);

        return filmMapper.toFilmResponse(film);
    }

    @PostMapping
    public FilmResponse addFilm(@RequestBody FilmRequest filmRequest) {
        log.info("Post /films - добавление нового фильма: name = {}", filmRequest.getName());
        Film film = filmMapper.toFilm(filmRequest);
        Film savedFilm = filmService.addFilm(film);
        log.info("Фильм добавлен: {}", film.getName());

        return filmMapper.toFilmResponse(savedFilm);
    }

    @PutMapping
    public FilmResponse updateFilm(@RequestBody FilmRequest filmRequest) {
        log.info("Put /films - обновление фильма: {}", filmRequest.getName());
        Film film = filmMapper.toFilm(filmRequest);
        Film updatedFilm = filmService.updateFilm(film);
        log.info("Фильм {} обновлен!", updatedFilm.getName());

        return filmMapper.toFilmResponse(updatedFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Put /films/{}/like/{} - добавление лайка", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Delete /films/{}/like/{} - удаление лайка", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmResponse> getPopularFilms(@RequestParam(required = false) Integer count) {
        log.info("Get /films/popular - получение списка популярных фильмов");
        List<Film> films = filmService.getPopularFilms(count);

        return films.stream()
                .map(filmMapper::toFilmResponse)
                .collect(Collectors.toList());
    }
}