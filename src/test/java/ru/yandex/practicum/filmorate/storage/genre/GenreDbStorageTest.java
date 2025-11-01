package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.mappers.GenreMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({GenreDbStorage.class, GenreMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    public void testFindAllGenres() {
        List<Genre> genres = genreStorage.findAll();

        assertThat(genres).hasSize(6);
        assertThat(genres)
                .extracting(Genre::getName)
                .containsExactly("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }

    @Test
    public void testFindGenreById() {
        Optional<Genre> genreOptional = genreStorage.findById(1L);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre.getId()).isEqualTo(1L);
                    assertThat(genre.getName()).isEqualTo("Комедия");
                });
    }

    @Test
    public void testFindGenreById2() {
        Optional<Genre> genreOptional = genreStorage.findById(4L);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre.getId()).isEqualTo(4L);
                    assertThat(genre.getName()).isEqualTo("Триллер");
                });
    }

    @Test
    public void testFindGenreByNonExistentId() {
        Optional<Genre> genreOptional = genreStorage.findById(999L);

        assertThat(genreOptional).isEmpty();
    }

    @Test
    public void testGenresOrder() {
        List<Genre> genres = genreStorage.findAll();

        // Проверяем что жанры отсортированы по ID
        assertThat(genres)
                .extracting(Genre::getId)
                .containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
    }

    @Test
    public void testGenreProperties() {
        Optional<Genre> genreOptional = genreStorage.findById(1L);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre).hasFieldOrPropertyWithValue("id", 1L);
                    assertThat(genre).hasFieldOrPropertyWithValue("name", "Комедия");
                    assertThat(genre).hasNoNullFieldsOrProperties();
                });
    }

    @Test
    public void testAllGenresHaveValidData() {
        List<Genre> genres = genreStorage.findAll();

        for (Genre genre : genres) {
            assertThat(genre.getId()).isNotNull();
            assertThat(genre.getName()).isNotBlank();
        }
    }
}