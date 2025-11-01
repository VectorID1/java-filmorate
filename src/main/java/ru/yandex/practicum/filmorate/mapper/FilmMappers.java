package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.request.FilmRequest;
import ru.yandex.practicum.filmorate.dto.response.FilmResponse;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmMappers {
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public Film toFilm(FilmRequest request) {
        Film film = new Film();
        film.setId(request.getId());
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        film.setMpa(request.getMpa());
        film.setGenreIds(request.getGenreIds() != null ? request.getGenreIds() : Set.of());

        return film;
    }

    public FilmResponse toFilmResponse(Film film) {
        FilmResponse response = new FilmResponse();
        response.setId(film.getId());
        response.setName(film.getName());
        response.setDescription(film.getDescription());
        response.setReleaseDate(film.getReleaseDate());
        response.setDuration(film.getDuration());
        response.setLikes(film.getLikes());

        Mpa mpa = film.getMpa() != null ?
                mpaStorage.findById(film.getMpa().getId()).orElse(null) : null;
        response.setMpa(mpa);

        List<Genre> genres = film.getGenreIds().stream()
                .map(genreId -> genreStorage.findById(genreId).orElse(null))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toList());
        response.setGenres(genres);

        return response;
    }
}