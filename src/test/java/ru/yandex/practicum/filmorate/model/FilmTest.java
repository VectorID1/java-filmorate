package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DateValidationException;
import ru.yandex.practicum.filmorate.exception.InvalidDurationException;
import ru.yandex.practicum.filmorate.exception.LengthExceededException;

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
    void addFilmValidData() {
        assertEquals(film.getName(), "testFilm", "Название фильма не совпадает");
        assertEquals(film.getDescription(), "testDescribtion123123123123123", "Описание не совпадает");
        assertEquals(film.getDescription().length(), 30);
        assertEquals(film.getDuration(), 100, "Продолжительность не совпадает");
        assertEquals(film.getReleaseDate(), LocalDate.of(2000, 10, 15), "Дата релиза" +
                " не совпадает");
    }

    @Test
    void nameFilmNoValid() {
        assertDoesNotThrow(() -> filmController.addFilm(film));
        film.setName("");
        assertThrows(ConditionsNotMetException.class, () -> filmController.addFilm(film));
        film.setName(null);
        assertThrows(ConditionsNotMetException.class, () -> filmController.addFilm(film));
    }

    @Test
    void describtionNoValid() {
        film.setDescription("jdjdjjhdfjklsdfklsdfuihuejnsdfkjbsdfgsdufjbsfbnskjdfdshfukjbsdfnsdbfhsdgfudskjfbsjdfbsds" +
                "sdkfbsdkjfbsdmfn sjdfhsdkjfnsdmf sdjkfbhsdjkfbnsndmf sdhfgsdjkfhsdjfbsdhfbjdfbmsdn fhdfsdhfbsdfjhsdb" +
                "skjfbsdkjfbskdjnfsdhfsdkhfbsdknfbhsdfbgshdbfsf nsjbsfjsbdfnsdbfjhdsbfnsd csjhcbhds");
        assertThrows(LengthExceededException.class, () -> filmController.addFilm(film));
    }

    @Test
    void dateReliseNoValid() {
        film.setReleaseDate(LocalDate.of(1700, 10, 25));
        assertThrows(DateValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void durationNoValid() {
        film.setDuration(-300);
        assertThrows(InvalidDurationException.class, () -> filmController.addFilm(film));
        film.setDuration(-1);
        assertThrows(InvalidDurationException.class, () -> filmController.addFilm(film));
        film.setDuration(0);
        assertThrows(InvalidDurationException.class, () -> filmController.addFilm(film));
    }

}