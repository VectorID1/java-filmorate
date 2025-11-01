package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {
    private FilmService filmService;
    private Film film;
    private Mpa mpa;

    @BeforeEach
    void setFilm() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        filmService = new FilmService(userStorage, filmStorage);
        film = new Film();
        mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("G");
        mpa.setDescription("123");
        film.setName("testFilm");
        film.setDescription("testDescribtion123123123123123");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(2000, 10, 15));
        film.setMpa(mpa);
    }


    @Test
    void nameFilmNoValid() {
        assertDoesNotThrow(() -> filmService.addFilm(film));
        film.setName("");
        assertThrows(ValidationException.class, () -> filmService.addFilm(film));
        film.setName(null);
        assertThrows(ValidationException.class, () -> filmService.addFilm(film));
    }

    @Test
    void descriptionNoValid() {
        film.setDescription("jdjdjjhdfjklsdfklsdfuihuejnsdfkjbsdfgsdufjbsfbnskjdfdshfukjbsdfnsdbfhsdgfudskjfbsjdfbsds" +
                "sdkfbsdkjfbsdmfn sjdfhsdkjfnsdmf sdjkfbhsdjkfbnsndmf sdhfgsdjkfhsdjfbsdhfbjdfbmsdn fhdfsdhfbsdfjhsdb" +
                "skjfbsdkjfbskdjnfsdhfsdkhfbsdknfbhsdfbgshdbfsf nsjbsfjsbdfnsdbfjhdsbfnsd csjhcbhds");
        assertThrows(ValidationException.class, () -> filmService.addFilm(film));
    }

    @Test
    void dateReleaseNoValid() {
        film.setReleaseDate(LocalDate.of(1700, 10, 25));
        assertThrows(ValidationException.class, () -> filmService.addFilm(film));
    }

    @Test
    void durationNoValid() {
        film.setDuration(-300);
        assertThrows(ValidationException.class, () -> filmService.addFilm(film));
        film.setDuration(-1);
        assertThrows(ValidationException.class, () -> filmService.addFilm(film));
        film.setDuration(0);
        assertThrows(ValidationException.class, () -> filmService.addFilm(film));
    }

}