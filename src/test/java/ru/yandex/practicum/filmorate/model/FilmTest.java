package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {
    private Film film;
    private FilmController filmController = new FilmController();

    @BeforeEach
    void setFilm() {
        film = new Film();
        film.setName("testFilm");
        film.setDescription("testDescribtion123123123123123");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(2000, 10, 15));
    }


    @Test
    void nameFilmNoValid() {
        assertDoesNotThrow(() -> filmController.addFilm(film));
        film.setName("");
        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
        film.setName(null);
        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void describtionNoValid() {
        film.setDescription("jdjdjjhdfjklsdfklsdfuihuejnsdfkjbsdfgsdufjbsfbnskjdfdshfukjbsdfnsdbfhsdgfudskjfbsjdfbsds" +
                "sdkfbsdkjfbsdmfn sjdfhsdkjfnsdmf sdjkfbhsdjkfbnsndmf sdhfgsdjkfhsdjfbsdhfbjdfbmsdn fhdfsdhfbsdfjhsdb" +
                "skjfbsdkjfbskdjnfsdhfsdkhfbsdknfbhsdfbgshdbfsf nsjbsfjsbdfnsdbfjhdsbfnsd csjhcbhds");
        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void dateReliseNoValid() {
        film.setReleaseDate(LocalDate.of(1700, 10, 25));
        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void durationNoValid() {
        film.setDuration(-300);
        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
        film.setDuration(-1);
        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
        film.setDuration(0);
        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

}