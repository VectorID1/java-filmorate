package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;


@Data
public class User {
    Long id;
    String login;
    String name;
    LocalDate birthday;
    String email;
}
