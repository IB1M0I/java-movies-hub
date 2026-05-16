package ru.practicum.moviehub.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.http.BaseHttpHandler;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

public class MoviesHandler extends BaseHttpHandler {
    MoviesStore store;
    Gson gson = new Gson();

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET":
                String path = exchange.getRequestURI().getPath();

                String json = gson.toJson(store.getAllMovies());

                if (path.split("/").length == 2) {

                    sendJson(exchange, 200, json);
                }
        }
    }

    private void getMovies() {

    }
}


