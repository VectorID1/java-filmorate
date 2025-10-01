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

    public Integer getLikeValue() {
        return likes.size();
    }
}
