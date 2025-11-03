package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.mappers.MpaMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaMapper mpaMapper;

    public MpaDbStorage(JdbcTemplate jdbcTemplate, MpaMapper mpaMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaMapper = mpaMapper;
    }

    @Override
    public List<Mpa> findAll() {
        String sql = "SELECT * FROM mpa ORDER BY id";
        return jdbcTemplate.query(sql, mpaMapper);
    }

    @Override
    public Optional<Mpa> findById(Long id) {
        String sql = "SELECT * FROM mpa WHERE id = ?";
        try {
            Mpa mpa = jdbcTemplate.queryForObject(sql, mpaMapper, id);
            return Optional.of(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsMpaById(Long id) {
        String sql = "SELECT COUNT(*) FROM mpa WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,id);
        return count > 0;
    }

    @Override
    public List<Mpa> findAllByIds(List<Long> mpaIds) {
        String placeholders = mpaIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = "SELECT * FROM mpa WHERE id IN (" + placeholders + ")";

        return jdbcTemplate.query(sql, mpaMapper, mpaIds.toArray());
    }

}
