package ru.yandex.practicum.filmorate.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRequest {
    private String name;
    private String login;
    private String email;
    private LocalDate birthday;
}
