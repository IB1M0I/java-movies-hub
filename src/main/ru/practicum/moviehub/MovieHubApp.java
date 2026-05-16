package ru.practicum.moviehub;

import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.http.MoviesServer;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

public class MovieHubApp {
    public static void main(String[] args) {
        //final MoviesServer server = new MoviesServer(new MoviesStore(), 8080);
        //Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        //server.start();



        try {
            final Movie movie1 = new Movie("Пираты", "Пираты в море", "Драма", 87, "Робертто",2000);
            final Movie movie2 = new Movie("Вторжение", "Космические захватчики", "Экшен", 120, "Дуррито",1999);

            MoviesStore store = new MoviesStore();
            store.addMovie(movie1);
            store.addMovie(movie2);

            final MoviesServer server = new MoviesServer(store, 8080);
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            server.start();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }


    }
}