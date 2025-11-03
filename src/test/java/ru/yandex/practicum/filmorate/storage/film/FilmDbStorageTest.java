package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.mappers.MpaMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.mappers.UserMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmMapper.class, MpaMapper.class, MpaDbStorage.class, UserDbStorage.class, UserMapper.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final MpaDbStorage mpaStorage;
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @Test
    void testSave() {
        Film film = createTestFilm("Test Film", mpaStorage.findById(1L).get());

        Film savedFilm = filmStorage.save(film);

        assertThat(savedFilm.getId()).isNotNull();
        assertThat(savedFilm.getName()).isEqualTo("Test Film");
        assertThat(savedFilm.getMpa().getId()).isEqualTo(1L);
    }

    @Test
    void testSaveGenres() {
        Film film = createTestFilm("Test Film", mpaStorage.findById(1L).get());
        film.setGenres(Set.of(
                createGenre(1L, "Комедия"),
                createGenre(2L, "Драма")
        ));

        Film savedFilm = filmStorage.save(film);

        Film foundFilm = filmStorage.findById(savedFilm.getId()).get();
        assertThat(foundFilm.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void testFindById() {
        Film film = filmStorage.save(createTestFilm("Test Film", mpaStorage.findById(1L).get()));

        Optional<Film> foundFilm = filmStorage.findById(film.getId());

        assertThat(foundFilm)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getId()).isEqualTo(film.getId());
                    assertThat(f.getName()).isEqualTo("Test Film");
                    assertThat(f.getMpa()).isNotNull();
                });
    }

    @Test
    void testFindByIdNoValid() {
        Optional<Film> foundFilm = filmStorage.findById(666L);

        assertThat(foundFilm).isEmpty();
    }

    @Test
    void testUpdate() {
        Film film = filmStorage.save(createTestFilm("Old Name", mpaStorage.findById(1L).get()));
        Film updatedFilm = new Film();
                updatedFilm.setId(film.getId());
                updatedFilm.setName("New Name");
                updatedFilm.setDescription("New Description");
                updatedFilm.setReleaseDate(film.getReleaseDate());
                updatedFilm.setDuration(150);
                updatedFilm.setMpa(mpaStorage.findById(2L).get());

        Film result = filmStorage.update(updatedFilm);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getDuration()).isEqualTo(150);
        assertThat(result.getMpa().getId()).isEqualTo(2L);
    }

    @Test
    void testUpdateNoValid() {
        Film nonExistentFilm = createTestFilm("Test", mpaStorage.findById(1L).get());
        nonExistentFilm.setId(666L);
        assertThatThrownBy(() -> filmStorage.update(nonExistentFilm))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testDelete() {
        Film film = filmStorage.save(createTestFilm("Test Film", mpaStorage.findById(1L).get()));

        filmStorage.delete(film.getId());

        Optional<Film> deletedFilm = filmStorage.findById(film.getId());
        assertThat(deletedFilm).isEmpty();
    }

    @Test
    void testFindAll() {
        filmStorage.save(createTestFilm("Film 1", mpaStorage.findById(1L).get()));
        filmStorage.save(createTestFilm("Film 2", mpaStorage.findById(2L).get()));

        List<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
        assertThat(films)
                .extracting(Film::getName)
                .containsExactly("Film 1", "Film 2");
    }

    @Test
    void testAddAndRemoveLike() {
        Film film = filmStorage.save(createTestFilm("Test Film", mpaStorage.findById(1L).get()));
        User user = userStorage.save(createTestUser("user@mail.com", "userlogin"));

        filmStorage.addLike(film.getId(), user.getId());

        Film filmWithLike = filmStorage.findById(film.getId()).get();
        assertThat(filmWithLike.getLikes()).contains(user.getId());

        filmStorage.removeLike(film.getId(), user.getId());

        Film filmWithoutLike = filmStorage.findById(film.getId()).get();
        assertThat(filmWithoutLike.getLikes()).doesNotContain(user.getId());
    }

    @Test
    void testFindPopularFilms() {
        Film film1 = filmStorage.save(createTestFilm("Film 1", mpaStorage.findById(1L).get()));
        Film film2 = filmStorage.save(createTestFilm("Film 2", mpaStorage.findById(1L).get()));
        Film film3 = filmStorage.save(createTestFilm("Film 3", mpaStorage.findById(1L).get()));

        User user1 = userStorage.save(createTestUser("user1@mail.com", "user1"));
        User user2 = userStorage.save(createTestUser("user2@mail.com", "user2"));
        User user3 = userStorage.save(createTestUser("user3@mail.com", "user3"));

        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user3.getId());

        List<Film> popularFilms = filmStorage.findPopularFilms(2);

        assertThat(popularFilms).hasSize(2);
        assertThat(popularFilms.get(0).getId()).isEqualTo(film2.getId());
        assertThat(popularFilms.get(1).getId()).isEqualTo(film1.getId());
    }

    @Test
    void testFindPopularFilmsLimit() {
        Film film1 = filmStorage.save(createTestFilm("Film 1", mpaStorage.findById(1L).get()));
        Film film2 = filmStorage.save(createTestFilm("Film 2", mpaStorage.findById(1L).get()));
        Film film3 = filmStorage.save(createTestFilm("Film 3", mpaStorage.findById(1L).get()));

        List<Film> popularFilms = filmStorage.findPopularFilms(3);

        assertThat(popularFilms).hasSize(3);
    }

    @Test
    void testLoadAllMpaForFilms() {
        Film film1 = filmStorage.save(createTestFilm("Test Film", mpaStorage.findById(1L).get()));
        Film film2 = filmStorage.save(createTestFilm("Test Film1", mpaStorage.findById(2L).get()));


        Film foundFilm1 = filmStorage.findById(film1.getId()).get();
        Film foundFilm2 = filmStorage.findById(film2.getId()).get();
        assertThat(foundFilm1.getMpa().getName()).isEqualTo("G");
        assertThat(foundFilm2.getMpa().getName()).isEqualTo("PG");

    }

    private Film createTestFilm(String name, Mpa mpa) {
        Film film = new Film();
            film.setName(name);
            film.setDescription("Test describtion");
            film.setReleaseDate(LocalDate.of(1990,2,14));
            film.setDuration(122);
            film.setMpa(mpa);
        return film;
    }

    private User createTestUser(String email, String login) {
        User user = new User();
        user.setName("Test user");
        user.setEmail(email);
        user.setLogin(login);
        user.setBirthday(LocalDate.of(1989,10,15));
        return user;
    }

    private Genre createGenre(Long id, String name) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        return genre;
    }
}