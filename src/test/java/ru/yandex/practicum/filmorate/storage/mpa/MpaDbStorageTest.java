package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testFindAll() {

        List<Mpa> allMpa = mpaStorage.findAll();

        assertThat(allMpa).hasSize(5);
        assertThat(allMpa)
                .extracting(Mpa::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    public void testFindMpaById() {
        Optional<Mpa> mpa = mpaStorage.findById(1L);

        assertThat(mpa)
                .isPresent()
                .hasValueSatisfying(m -> {
                    assertThat(m.getId()).isEqualTo(1L);
                    assertThat(m.getName()).isEqualTo("G");
                });
    }

    @Test
    public void testFindMpaById3() {
        Optional<Mpa> mpa = mpaStorage.findById(3L);

        assertThat(mpa)
                .isPresent()
                .hasValueSatisfying(m -> {
                    assertThat(m.getId()).isEqualTo(3L);
                    assertThat(m.getName()).isEqualTo("PG-13");
                });
    }

    @Test
    public void testFindMpaByNonExistentId() {
        Optional<Mpa> mpaOptional = mpaStorage.findById(100L);

        assertThat(mpaOptional).isEmpty();
    }

    @Test
    public void testFindByIds() {
        List<Mpa> mpaList = mpaStorage.findAllByIds(List.of(1L, 2L, 3L));

        assertThat(mpaList).hasSize(3);
        assertThat(mpaList)
                .extracting(Mpa::getId)
                .containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    public void testMpaOrder() {
        List<Mpa> mpaList = mpaStorage.findAll();

        assertThat(mpaList)
                .extracting(Mpa::getId)
                .containsExactly(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    public void testMpaProperties() {
        Optional<Mpa> mpa = mpaStorage.findById(1L);

        assertThat(mpa)
                .isPresent()
                .hasValueSatisfying(m -> {
                    assertThat(m).hasFieldOrPropertyWithValue("id", 1L);
                    assertThat(m).hasFieldOrPropertyWithValue("name", "G");
                    assertThat(m).hasNoNullFieldsOrProperties();
                });
    }

    @Test
    public void findByAllWhenEmptyList() {
        List<Mpa> mpa = mpaStorage.findAllByIds(List.of());

        assertThat(mpa).isEmpty();
    }

    @Test
    public void findByAllWhenIncorrectId() {
        List<Mpa> mpa = mpaStorage.findAllByIds(List.of(100L, 105L));

        assertThat(mpa).isEmpty();
    }
}