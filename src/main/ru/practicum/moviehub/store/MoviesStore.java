package ru.practicum.moviehub.store;


import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.List;

//Хранилище фильмов
public class MoviesStore {
    private List<Movie> movies = new ArrayList<>();


    public void addMovie(Movie movie) {
            movies.add(movie);
    }

    public List<Movie> getAllMovies() {
        return movies;
    }

    public void clear(){
        movies.clear();
        Movie.clearID();
    }

}