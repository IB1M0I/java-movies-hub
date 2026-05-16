package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080"; // !!! добавьте базовую часть URL
    private static MoviesServer server;
    private static HttpClient client;
    private static Gson gson;
    private static MoviesStore movies;

    private static final Movie movie1 = new Movie("Пираты","Пираты в море","Драма",87,"Робертто");
    private static final Movie movie2 =  new Movie("Вторжение","Космические захватчики", "Экшен",120,"Дуррито");

    @BeforeAll
    static void beforeAll() {
        // !!! Реализуйте метод beforeAll
        movies = new MoviesStore();
        movies.addMovie(movie1);
        movies.addMovie(movie2);

        gson = new Gson();
        server = new MoviesServer(movies,8080);
        server.start();
        client = HttpClient.newBuilder().connectTimeout(Duration.ofDays(2)).build();
    }

    @AfterAll
    static void afterAll() {
        // !!! Реализуйте метод afterAll
        server.stop();
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        MoviesStore emptyStore = new MoviesStore();
        MoviesServer emptyServer = new MoviesServer(emptyStore,8081);
        emptyServer.start();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/movies")) // !!! Добавьте правильный URI
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        emptyServer.stop();

        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals(200, response.statusCode(), "GET /movies должен вернуть 200");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = response.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void getMovies_returnArray() {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        try {
            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
            String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");

            String body = response.body().trim();

            assertEquals(200, response.statusCode(), "GET /movies должен вернуть 200");
            assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                    "Content-Type должен содержать формат данных и кодировку");
            assertEquals(List.of(movie1,movie2),gson.fromJson(response.body(),new ListOfMoviesTypeToken().getType()));
            assertTrue(body.startsWith("[")&& body.endsWith("]"),"Ожидается JSON-массив");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}