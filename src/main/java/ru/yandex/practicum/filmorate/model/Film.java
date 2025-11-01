package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    Long id;
    String description;
    LocalDate releaseDate;
    Integer duration;
    String name;
    Set<Long> likes = new HashSet<>();
    Set<Long> genreIds = new HashSet<>();
    Mpa mpa;

    public Integer getLikeValue() {
        return likes.size();
    }


}
