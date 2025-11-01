package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.mappers.UserMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({FilmDbStorage.class, FilmMapper.class, UserDbStorage.class, UserMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;  // ← ДОБАВИЛИ для создания пользователей

    @Test
    public void testFindFilmById() {
        Film film = createTestFilm();
        Film savedFilm = filmStorage.save(film);

        Optional<Film> filmOptional = filmStorage.findById(savedFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(foundFilm ->
                        assertThat(foundFilm).hasFieldOrPropertyWithValue("id", savedFilm.getId())
                );
    }

    @Test
    public void testSaveFilm() {
        Film film = createTestFilm();

        Film savedFilm = filmStorage.save(film);

        assertThat(savedFilm.getId()).isNotNull();
        assertThat(savedFilm.getName()).isEqualTo("Test Film");
    }

    @Test
    public void testUpdateFilm() {
        Film film = createTestFilm();
        Film savedFilm = filmStorage.save(film);

        savedFilm.setName("Updated Film");
        Film updatedFilm = filmStorage.update(savedFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
    }

    @Test
    public void testFindAllFilms() {
        Film film1 = createTestFilm();
        Film film2 = createTestFilm();
        film2.setName("Another Film");

        filmStorage.save(film1);
        filmStorage.save(film2);

        List<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
    }

    @Test
    public void testSaveFilmWithGenres() {
        Film film = createTestFilm();
        film.setGenreIds(Set.of(1L, 2L));

        Film savedFilm = filmStorage.save(film);

        Optional<Film> foundFilm = filmStorage.findById(savedFilm.getId());
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getGenreIds()).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    public void testAddLike() {
        User user = createTestUser("user@mail.com", "testuser");
        User savedUser = userStorage.save(user);

        Film film = createTestFilm();
        Film savedFilm = filmStorage.save(film);

        filmStorage.addLike(savedFilm.getId(), savedUser.getId());

        Optional<Film> foundFilm = filmStorage.findById(savedFilm.getId());
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getLikes()).contains(savedUser.getId());
    }

    @Test
    public void testRemoveLike() {
        User user = createTestUser("user@mail.com", "testuser");
        User savedUser = userStorage.save(user);

        Film film = createTestFilm();
        Film savedFilm = filmStorage.save(film);

        filmStorage.addLike(savedFilm.getId(), savedUser.getId());

        filmStorage.removeLike(savedFilm.getId(), savedUser.getId());

        Optional<Film> foundFilm = filmStorage.findById(savedFilm.getId());
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getLikes()).isEmpty();
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        film.setMpa(mpa);

        return film;
    }

    private User createTestUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}