package ru.practicum.moviehub.handler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.http.BaseHttpHandler;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoviesHandler extends BaseHttpHandler {
    MoviesStore store;
    Gson gson = new Gson();

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange)  {
        try {

            String path = exchange.getRequestURI().getPath();
            String[] strSplit = path.split("/");
            switch (exchange.getRequestMethod()) {
                case "GET":
                    if (strSplit.length == 2) {
                        getAllMovies(exchange);
                    } else if (strSplit.length == 3) {
                        getMovieID(exchange, strSplit[2]);
                    }
                    break;
                case "POST":
                    addNewMovie(exchange);
                    break;
                case "DELETE":
                    if(strSplit.length == 3){}
                    deleteMovie(exchange,strSplit[2]);
                    break;
            }
        } catch (ErrorResponse e) {
            try {
                sendJson(exchange, e.getCode(), e.getError());
            } catch (IOException ex) {
                System.out.println("Ошибка отправки");
            }
        } catch (IOException e){
            System.out.println("Ошибка отправки");
        }
    }

    private void getAllMovies(HttpExchange exchange) throws IOException {
        String json = gson.toJson(store.getAllMovies());
        sendJson(exchange, 200, json);
    }

    private void getMovieID(HttpExchange exchange, String strID) throws ErrorResponse {
        try {
            int id = Integer.parseInt(strID);

            Optional<Movie> optionalMovie = store.getAllMovies().stream()
                    .filter(m -> m.getID() == id)
                    .findFirst();

            if(optionalMovie.isPresent()){
                Movie movie = optionalMovie.get();
                sendJson(exchange,200,gson.toJson(movie));
            }else{
                throw new ErrorResponse("Фильм не найден",404);
            }
        } catch (NumberFormatException e) {
            throw new ErrorResponse("Некорректный ID",400);
        } catch (IOException e) {
            System.out.println("Ошибка отправки");;
        }

    }

    private void addNewMovie(HttpExchange exchange) throws IOException, ErrorResponse {
        Movie newMovie;

        try (InputStream is = exchange.getRequestBody()) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);


            List<String> error = new ArrayList<>();


            JsonElement jsonElement = JsonParser.parseString(json);
            JsonObject jsonObject;
            if (jsonElement.isJsonObject()) {
                jsonObject = jsonElement.getAsJsonObject();
            } else {
                throw new ErrorResponse("Неправильный JSON", 400);//TODO
            }

            if (jsonObject.get("title").getAsString().length() >= 100) {
                error.add("Название не должно быть больше 100 символов");
            }
            if (jsonObject.get("title").getAsString().isEmpty()) {
                error.add("Название не должно быть пустым");
            }
            if (jsonObject.get("year").getAsInt() < 1888 || jsonObject.get("year").getAsInt() > LocalDate.now().getYear() + 1) {
                error.add("Год должен быть от 1888 до " + LocalDate.now().getYear() + 1);
            }
            List<String> headers = exchange.getRequestHeaders().get("Content-Type");
            if (headers == null || !headers.contains("application/json")) {
                throw new ErrorResponse("Неподдерживаемый тип данных", 415);
            }

            if (!error.isEmpty()) {
                JsonObject errorObject = new JsonObject();
                errorObject.addProperty("error", "Ошибка валидации");
                errorObject.add("details", gson.toJsonTree(error));


                throw new ErrorResponse(gson.toJson(errorObject), 422);
            }

            newMovie = gson.fromJson(json, Movie.class);
            store.addMovie(newMovie);
            sendJson(exchange, 201, gson.toJson(newMovie));
            System.out.println("Фильм добавлен");
        }



    }

    private void deleteMovie(HttpExchange exchange, String strID) throws ErrorResponse {
        try {
            int id = Integer.parseInt(strID);

            if(store.delet(id)){
                sendNoContent(exchange);
            }else{
                throw new ErrorResponse("Фильм не найден",404);
            }

        }catch (NumberFormatException e){
            throw new ErrorResponse("Некорректный ID",400);
        } catch (IOException e) {
            System.out.println("Ошибка отправки ответа");
        }
    }
}


