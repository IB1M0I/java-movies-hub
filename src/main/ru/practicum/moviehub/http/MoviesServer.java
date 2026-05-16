package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.handler.MoviesHandler;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private final HttpServer server;
    MoviesStore store;

    public MoviesServer(MoviesStore store, int port) throws IOException {
        this.store = store;


            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/movies",new MoviesHandler(store));
    }

    public void start() {
        // запустите сервер
        server.start();
        System.out.println("Сервер запущен");
    }

    public void stop() {
        // остановите сервер
        server.stop(0);
        System.out.println("Сервер остановлен");
    }
}