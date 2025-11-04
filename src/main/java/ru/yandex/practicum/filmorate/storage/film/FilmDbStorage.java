package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Primary
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final MpaStorage mpaStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper, MpaStorage mpaStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
        this.mpaStorage = mpaStorage;
    }

    @Override
    public Film save(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        film.setId(generatedId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film.getId(), film.getGenres());
        }
        return film;
    }

    private void saveGenres(Long filmId, Set<Genre> genres) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        for (Genre genre : genres) {
            jdbcTemplate.update(sql, filmId, genre.getId());
        }
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        if (rowsUpdated == 0) {
            throw new NotFoundException("Не удалось обновить данные");
        }
        updateFilmGenres(film.getId(), film.getGenres());
        return findById(film.getId()).orElse(film);
    }

    private void updateFilmGenres(Long filmId, Set<Genre> genres) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);

        if (genres != null && !genres.isEmpty()) {
            String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : genres) {
                jdbcTemplate.update(insertSql, filmId, genre.getId());
            }
        }
    }

    @Override
    public void delete(Long filmId) {
        String sql = "DELETE FROM films WHERE id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, filmId);

        if (rowsDeleted == 0) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            film.setLikes(loadLikes(id));
            film.setGenres(loadGenres(id));
            film.setMpa(loadMpaForFilm(film));
            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        loadAllLikesForFilms(films);
        loadAllGenresForFilms(films);
        loadAllMpaForFilms(films);
        return films;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> findPopularFilms(int limit) {
        String sql = """
                SELECT f.*, COUNT(l.user_id) as likes_count
                FROM films f
                LEFT JOIN film_likes l ON f.id = l.film_id
                GROUP BY f.id
                ORDER BY likes_count DESC
                LIMIT ?
                """;
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, limit);
        loadAllLikesForFilms(films);
        loadAllGenresForFilms(films);

        return films;
    }

    private void loadAllLikesForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        Map<Long, Set<Long>> likesMap = loadAllLikes(
                films.stream().map(Film::getId).toList()
        );

        films.forEach(film ->
                film.setLikes(likesMap.getOrDefault(film.getId(), Set.of()))
        );
    }

    private Map<Long, Set<Long>> loadAllLikes(List<Long> filmIds) {
        if (filmIds.isEmpty()) return Map.of();

        String sql = "SELECT film_id, user_id FROM film_likes WHERE film_id IN (" +
                filmIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ")";

        return jdbcTemplate.query(sql, (ResultSetExtractor<Map<Long, Set<Long>>>) rs -> {
            Map<Long, Set<Long>> result = new HashMap<>();
            while (rs.next()) {
                result.computeIfAbsent(rs.getLong("film_id"), k -> new HashSet<>())
                        .add(rs.getLong("user_id"));
            }
            return result;
        }, filmIds.toArray());
    }

    private Set<Long> loadLikes(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.queryForList(sql, Long.class, filmId);
        return new HashSet<>(likes);
    }

    private Map<Long, Set<Genre>> loadAllGenres(List<Long> filmIds) {
        if (filmIds.isEmpty()) return Map.of();

        String sql = """
                SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id IN (""" +
                filmIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ")";

        return jdbcTemplate.query(sql, (ResultSetExtractor<Map<Long, Set<Genre>>>) rs -> {
            Map<Long, Set<Genre>> result = new HashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");

                Genre genre = new Genre();
                genre.setId(rs.getLong("genre_id"));
                genre.setName(rs.getString("genre_name"));

                result.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
            }
            return result;
        }, filmIds.toArray());
    }

    private void loadAllGenresForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        Map<Long, Set<Genre>> genresMap = loadAllGenres(
                films.stream().map(Film::getId).toList()
        );

        films.forEach(film ->
                film.setGenres(genresMap.getOrDefault(film.getId(), Set.of()))
        );
    }

    private Set<Genre> loadGenres(Long filmId) {
        String sql = """
                SELECT g.id, g.name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                """;

        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getLong("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, filmId);

        return new HashSet<>(genres);
    }

    private Mpa loadMpaForFilm(Film film) {
        if (film.getMpa() == null) return null;

        return mpaStorage.findById(film.getMpa().getId())
                .orElse(null);
    }

    private void loadAllMpaForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        List<Long> mpaIds = films.stream()
                .map(film -> film.getMpa().getId())
                .distinct()
                .collect(Collectors.toList());

        if (mpaIds.isEmpty()) return;

        Map<Long, Mpa> mpaMap = mpaStorage.findAllByIds(mpaIds).stream()
                .collect(Collectors.toMap(Mpa::getId, mpa -> mpa));

        films.forEach(film -> {
            Mpa fullMpa = mpaMap.get(film.getMpa().getId());
            film.setMpa(fullMpa);
        });
    }
}

