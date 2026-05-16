package ru.practicum.moviehub.model;


import ru.practicum.moviehub.api.ErrorResponse;

import java.time.LocalDate;
import java.util.Objects;

//Модель фильма
public class Movie {
    private String title; //Название
    private String description; //Описание
    private String genre; //Жанр
    private int duration; //Длительность
    private String director;
    private int year;

    private static int nextID = 1;
    private final int ID = nextID++;

    public Movie(String title, String description, String genre, int duration, String director, int year) throws IllegalArgumentException{
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.duration = duration;
        this.director = director;


        if (checkYear(year)) {
            this.year = year;
        }else{

        }

    }

    public Movie(){}

    public static void clearID(){
        nextID = 1;
    }

    public boolean checkYear(int year){
        return year > 1888 && year < LocalDate.now().getYear() + 1;
    }
    public boolean checkYear(){
        return this.year > 1888 && this.year < LocalDate.now().getYear() + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return duration == movie.duration && year == movie.year && ID == movie.ID && Objects.equals(title, movie.title) && Objects.equals(description, movie.description) && Objects.equals(genre, movie.genre) && Objects.equals(director, movie.director);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, genre, duration, director, year, ID);
    }
}