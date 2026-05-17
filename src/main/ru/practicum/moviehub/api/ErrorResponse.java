package ru.practicum.moviehub.api;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Ошибка ответа
public class ErrorResponse extends Exception {
    String error;
    int code;
    LocalDateTime timestamp;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public ErrorResponse(String error, int code) {
        this.error = error;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }

    public String getError() {
        return error;
    }

    public int getCode() {
        return code;

    }

    public String getTimestamp() {
        return timestamp.format(formatter);
    }


}