package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.mappers.GenreRowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({GenreDbStorage.class, GenreRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    public void testFindAllGenres() {
        List<Genre> genres = genreStorage.findAll();

        assertThat(genres).hasSize(6);
        assertThat(genres).extracting(Genre::getName).containsExactly("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }

    @Test
    public void testFindGenreById() {
        Optional<Genre> genreOptional = genreStorage.findById(1L);

        assertThat(genreOptional).isPresent().hasValueSatisfying(genre -> {
            assertThat(genre.getId()).isEqualTo(1L);
            assertThat(genre.getName()).isEqualTo("Комедия");
        });
    }

    @Test
    public void testFindGenreById1() {
        Optional<Genre> genre = genreStorage.findById(1L);

        assertThat(genre).isPresent().hasValueSatisfying(g -> {
            assertThat(g.getId()).isEqualTo(1L);
            assertThat(g.getName()).isEqualTo("Комедия");
        });
    }

    @Test
    public void testFindGenreByNonExistentId() {
        Optional<Genre> genreOptional = genreStorage.findById(105L);

        assertThat(genreOptional).isEmpty();
    }

    @Test
    public void testFindAll() {
        List<Genre> genres = genreStorage.findAll();

        assertThat(genres).extracting(Genre::getName).containsExactly("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }

    @Test
    public void testGenreProperties() {
        Optional<Genre> genreOptional = genreStorage.findById(1L);

        assertThat(genreOptional).isPresent().hasValueSatisfying(genre -> {
            assertThat(genre).hasFieldOrPropertyWithValue("id", 1L);
            assertThat(genre).hasFieldOrPropertyWithValue("name", "Комедия");
            assertThat(genre).hasNoNullFieldsOrProperties();
        });
    }

    @Test
    public void testGenreOrderShouldBeSortedById() {
        List<Genre> genres = genreStorage.findAll();

        assertThat(genres).extracting(Genre::getId).containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
    }
}