package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.mappers.UserMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Primary
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;

    public UserDbStorage(JdbcTemplate jdbcTemplate, UserMapper userMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        String sql = "INSERT INTO users (name, login, birthday, email) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getName());
            ps.setString(2, user.getLogin());
            ps.setDate(3, Date.valueOf(user.getBirthday()));
            ps.setString(4, user.getEmail());
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET name = ?, login = ?, birthday = ?, email = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                user.getName(),
                user.getLogin(),
                user.getBirthday(),
                user.getEmail(),
                user.getId()
        );

        if (rowsUpdated == 0) {
            throw new NotFoundException("Не удалось обновить данные");
        }

        return user;
    }

    @Override
    public void delete(Long userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, userId);

        if (rowsDeleted == 0) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userMapper, id);
            loadFriends(user);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, userMapper);
        for (User user : users) {
            loadFriends(user);
        }
        return users;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (findById(userId).isEmpty() || findById(friendId).isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }

        String sqlCheck = "SELECT COUNT(*) FROM user_friends WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sqlCheck, Integer.class, userId, friendId);

        if (count > 0) {
            throw new ValidationException("Пользователь уже в друзьях");
        }

        String sql = "INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        if (findById(userId).isEmpty() || findById(friendId).isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }

        String sql = "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> findAllByIds(List<Long> userIds) {
        if (userIds.isEmpty()) return List.of();

        String placeholders = userIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT * FROM users WHERE id IN (" + placeholders + ")";

        return jdbcTemplate.query(sql, userMapper, userIds.toArray());
    }

    private void loadFriends(User user) {
        Long userId = user.getId();
        String sqlFriends = "SELECT friend_id FROM user_friends WHERE user_id = ?";
        List<Long> friends = jdbcTemplate.queryForList(sqlFriends, Long.class, userId);
        user.setFriends(new HashSet<>(friends));

    }
}