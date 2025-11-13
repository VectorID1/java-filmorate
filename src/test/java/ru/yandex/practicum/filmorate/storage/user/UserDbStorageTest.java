package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    public void testSaveUserWithGeneratedId() {

        User user = createTestUser("test@mail.com", "testLogin");

        User savedUser = userStorage.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@mail.com");
        assertThat(savedUser.getId()).isEqualTo(user.getId());

    }


    @Test
    public void testUpdateUser() {
        User user = createTestUser("update@mail.com", "updateLogin");
        User savedUser = userStorage.save(user);

        savedUser.setName("Updated Name");
        savedUser.setEmail("newEmail@mail.com");
        User updatedUser = userStorage.update(savedUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("newEmail@mail.com");
    }

    @Test
    public void testFindUserById() {
        User user1 = createTestUser("user1@mail.com", "user1");
        User user2 = createTestUser("user2@mail.com", "user2");
        User user3 = createTestUser("user3@mail.com", "user3");

        User savedUser1 = userStorage.save(user1);
        userStorage.save(user2);
        userStorage.save(user3);

        Optional<User> userInDb = userStorage.findById(savedUser1.getId());
        assertThat(userInDb).isPresent();
        assertThat(userInDb).hasValueSatisfying(user -> {
            assertThat(user.getName()).isEqualTo(savedUser1.getName());
            assertThat(user.getEmail()).isEqualTo(savedUser1.getEmail());
            assertThat(user.getId()).isEqualTo(savedUser1.getId());
        });

    }

    @Test
    public void testFindAllUsers() {
        User user1 = createTestUser("user1@mail.com", "user1");
        User user2 = createTestUser("user2@mail.com", "user2");
        User user3 = createTestUser("user3@mail.com", "user3");
        User user4 = createTestUser("user4@mail.com", "user4");

        userStorage.save(user1);
        userStorage.save(user2);
        userStorage.save(user3);
        userStorage.save(user4);


        List<User> users = userStorage.findAll();

        assertThat(users).hasSize(4);
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
        user.setName("Default name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}