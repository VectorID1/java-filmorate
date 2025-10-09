package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.Getter;

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
    Set<Genre> genres = new HashSet<>();
    Mpa mpa;

    public Integer getLikeValue() {
        return likes.size();
    }

    @Getter
    public enum Genre {
        COMEDY("Комедия"),
        DRAMA("Драма"),
        CARTOON("Мультфильм"),
        THRILLER("Триллер"),
        DOCUMENTARY("Документальный"),
        ACTION("Боевик");

        private final String displayGenre;

        Genre(String displayGenre) {
            this.displayGenre = displayGenre;
        }
    }


    @Getter
    public enum Mpa {
        G("нет возрастных ограничений"),
        PG("детям рекомендуется смотреть фильм с родителями"),
        PG_13("детям до 13 лет просмотр не желателен"),
        R("лицам до 17 лет просматривать фильм можно только в присутствии взрослого"),
        NC_17("лицам до 18 лет просмотр запрещён");

        private final String mpaDiscription;

        Mpa(String mpaDiscription) {
            this.mpaDiscription = mpaDiscription;
        }

    }
}
