package ru.practicum.moviehub;

import ru.practicum.moviehub.http.MoviesServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

public class MovieHubApp {
    public static void main(String[] args) {
        final MoviesServer server;
        try {
            server = new MoviesServer(new MoviesStore(), 8080);
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            server.start();
        } catch (IOException e) {
            System.out.println("Ошибка создания сервера");
        }


    }
}