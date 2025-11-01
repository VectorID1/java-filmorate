package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.mappers.UserMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({UserDbStorage.class, UserMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    public void testFindUserById() {
        User user = createTestUser("test@mail.com", "testlogin");
        User savedUser = userStorage.save(user);

        Optional<User> userOptional = userStorage.findById(savedUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(foundUser ->
                        assertThat(foundUser).hasFieldOrPropertyWithValue("id", savedUser.getId())
                );
    }

    @Test
    public void testSaveUser() {
        User user = createTestUser("save@mail.com", "savelogin");

        User savedUser = userStorage.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("save@mail.com");
    }

    @Test
    public void testUpdateUser() {
        User user = createTestUser("update@mail.com", "updatelogin");
        User savedUser = userStorage.save(user);

        savedUser.setName("Updated Name");
        User updatedUser = userStorage.update(savedUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    public void testFindAllUsers() {
        User user1 = createTestUser("user1@mail.com", "user1");
        User user2 = createTestUser("user2@mail.com", "user2");

        userStorage.save(user1);
        userStorage.save(user2);

        List<User> users = userStorage.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    public void testAddFriend() {
        User user1 = createTestUser("user1@mail.com", "user1");
        User user2 = createTestUser("user2@mail.com", "user2");

        User savedUser1 = userStorage.save(user1);
        User savedUser2 = userStorage.save(user2);

        userStorage.addFriend(savedUser1.getId(), savedUser2.getId());

        Optional<User> foundUser = userStorage.findById(savedUser1.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFriends()).contains(savedUser2.getId());
    }

    @Test
    public void testRemoveFriend() {
        User user1 = createTestUser("user1@mail.com", "user1");
        User user2 = createTestUser("user2@mail.com", "user2");

        User savedUser1 = userStorage.save(user1);
        User savedUser2 = userStorage.save(user2);

        userStorage.addFriend(savedUser1.getId(), savedUser2.getId());
        userStorage.removeFriend(savedUser1.getId(), savedUser2.getId());

        Optional<User> foundUser = userStorage.findById(savedUser1.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFriends()).isEmpty();
    }

    @Test
    public void testDeleteUser() {
        User user = createTestUser("delete@mail.com", "deletelogin");
        User savedUser = userStorage.save(user);

        userStorage.delete(savedUser.getId());

        Optional<User> deletedUser = userStorage.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    public void testUserNameAutoFill() {
        User user = createTestUser("auto@mail.com", "autologin");
        user.setName("");

        User savedUser = userStorage.save(user);

        assertThat(savedUser.getName()).isEqualTo("autologin");
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