package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.mappers.MpaMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({MpaDbStorage.class, MpaMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    public void testFindAllMpa() {
        List<Mpa> mpaList = mpaStorage.findAll();

        assertThat(mpaList).hasSize(5);
        assertThat(mpaList)
                .extracting(Mpa::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    public void testFindMpaById() {
        Optional<Mpa> mpaOptional = mpaStorage.findById(1L);

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa -> {
                    assertThat(mpa.getId()).isEqualTo(1L);
                    assertThat(mpa.getName()).isEqualTo("G");
                });
    }

    @Test
    public void testFindMpaById3() {
        Optional<Mpa> mpaOptional = mpaStorage.findById(3L);

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa -> {
                    assertThat(mpa.getId()).isEqualTo(3L);
                    assertThat(mpa.getName()).isEqualTo("PG-13");
                });
    }

    @Test
    public void testFindMpaByNonExistentId() {
        Optional<Mpa> mpaOptional = mpaStorage.findById(999L);

        assertThat(mpaOptional).isEmpty();
    }

    @Test
    public void testMpaOrder() {
        List<Mpa> mpaList = mpaStorage.findAll();

        // Проверяем что MPA отсортированы по ID
        assertThat(mpaList)
                .extracting(Mpa::getId)
                .containsExactly(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    public void testMpaProperties() {
        Optional<Mpa> mpaOptional = mpaStorage.findById(1L);

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa -> {
                    assertThat(mpa).hasFieldOrPropertyWithValue("id", 1L);
                    assertThat(mpa).hasFieldOrPropertyWithValue("name", "G");
                    assertThat(mpa).hasNoNullFieldsOrProperties();
                });
    }
}