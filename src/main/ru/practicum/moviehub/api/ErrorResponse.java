package ru.practicum.moviehub.api;


import java.time.LocalDate;
import java.time.LocalDateTime;

//Ошибка ответа
public class ErrorResponse extends Exception {
    String error;
    int code;
    LocalDateTime timestamp;

    public ErrorResponse(String error, int code) {
        this.error = error;
        this.code = code;
        this.timestamp = timestamp;
    }

    public String getError() {
        return error;
    }

    public int getCode() {
        return code;
    }
}