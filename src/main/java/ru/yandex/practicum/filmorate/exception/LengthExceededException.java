package ru.yandex.practicum.filmorate.exception;

public class LengthExceededException extends RuntimeException {
    public LengthExceededException(String message) {
        super(message);
    }
}
