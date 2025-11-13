package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.mappers.GenreRowMapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    public GenreDbStorage(JdbcTemplate jdbcTemplate, GenreRowMapper genreRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreRowMapper = genreRowMapper;
    }

    @Override
    public List<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, genreRowMapper);
    }

    @Override
    public Optional<Genre> findById(Long id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        try {
            Genre genre = jdbcTemplate.queryForObject(sql, genreRowMapper, id);
            return Optional.of(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void validateGenresExist(Set<Genre> genres) {
        if (genres != null && !genres.isEmpty()) {
            List<Long> genreIds = genres.stream().map(Genre::getId).collect(Collectors.toList());
            String placeholders = genreIds.stream().map(id -> "?").collect(Collectors.joining(","));
            String sql = "SELECT COUNT(*) FROM genres WHERE id IN (" + placeholders + ")";

            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genreIds.toArray());
            if (count == null || count != genreIds.size()) {
                throw new NotFoundException("Один или несколько жанров не найдены");
            }
        }
    }
}
