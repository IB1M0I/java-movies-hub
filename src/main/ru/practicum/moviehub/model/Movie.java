package ru.practicum.moviehub.model;


import java.util.Objects;

//Модель фильма
public class Movie {
    private String name; //Название
    private String description; //Описание
    private String genre; //Жанр
    private int duration; //Длительность
    private String director;

    public Movie(String name, String description, String genre, int duration, String director) {
        this.name = name;
        this.description = description;
        this.genre = genre;
        this.duration = duration;
        this.director = director;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return duration == movie.duration && Objects.equals(name, movie.name) && Objects.equals(description, movie.description) && Objects.equals(genre, movie.genre) && Objects.equals(director, movie.director);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, genre, duration, director);
    }
}