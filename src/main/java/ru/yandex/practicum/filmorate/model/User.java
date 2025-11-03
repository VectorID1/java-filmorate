package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Data
public class User {
    Long id;
    String login;
    String name;
    LocalDate birthday;
    String email;
    Set<Long> friends = new HashSet<>();
}
