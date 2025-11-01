package ru.yandex.practicum.filmorate.dto.request;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class FilmRequest {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Mpa mpa;
    private Set<Long> genreIds;
    private Set<Genre> genres;

    public Set<Long> getGenreIds() {
        if (genreIds != null && !genreIds.isEmpty()) {
            return genreIds;
        }
        if (genres != null && !genres.isEmpty()) {
            return genres.stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }
}