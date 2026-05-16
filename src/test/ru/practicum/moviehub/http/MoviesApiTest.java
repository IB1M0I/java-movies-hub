package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080"; // !!! добавьте базовую часть URL
    private static MoviesServer server;
    private static HttpClient client;
    private static Gson gson = new  Gson();
    private static MoviesStore movies;

    private static final Movie movie1 = new Movie("Пираты", "Пираты в море", "Драма", 87, "Робертто", 2000);
    private static final Movie movie2 = new Movie("Вторжение", "Космические захватчики", "Экшен", 120, "Дуррито", 1999);

    @BeforeAll
    static void beforeAll() throws IOException {
        // !!! Реализуйте метод beforeAll
        movies = new MoviesStore();
        movies.addMovie(movie1);
        movies.addMovie(movie2);
        server = new MoviesServer(movies, 8080);
        server.start();
        client = HttpClient.newBuilder().connectTimeout(Duration.ofDays(2)).build();
    }

    @AfterAll
    static void afterAll() {
        // !!! Реализуйте метод afterAll
        server.stop();
    }

    @BeforeEach
    void beforeEach() {
        movies.clear(); // очищаем хранилище
        Movie movie1 = new Movie("Пираты", "Пираты в море", "Драма", 87, "Робертто", 2000);
        Movie movie2 = new Movie("Вторжение", "Космические захватчики", "Экшен", 120, "Дуррито", 1999);

        movies.addMovie(movie1);
        movies.addMovie(movie2);
    }


    //getMovies с пустым списком вернет код200
    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        MoviesStore emptyStore = new MoviesStore();
        MoviesServer emptyServer = new MoviesServer(emptyStore, 8081);
        emptyServer.start();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/movies")) // !!! Добавьте правильный URI
                .GET()
                .header("Content-Type","application/json")
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

    //getMovies вернет заполненный список и код 200
    @Test
    void getMovies_returnArray() {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type","application/json")
                .build();

        try {
            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
            String contentTypeHeaderValue = response.headers().firstValue("Content-Type").orElse("");

            String body = response.body().trim();

            assertEquals(200, response.statusCode(), "GET /movies должен вернуть 200");
            assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                    "Content-Type должен содержать формат данных и кодировку");
            assertEquals(List.of(movie1, movie2), gson.fromJson(response.body(), new ListOfMoviesTypeToken().getType()));
            assertTrue(body.startsWith("[") && body.endsWith("]"), "Ожидается JSON-массив");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    //postMovies успешное добавление поста с кодом 201
    @Test
    void postMovies_returnCode201() throws IOException, InterruptedException {
        String post = "{\"title\":\"Ромарио\",\"description\":\"Приключение Ромарио\",\"genre\":\"Комедия\",\"duration\":110,\"director\":\"Паприкко\",\"year\":2026}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(post))
                .header("Content-Type","application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals(201, response.statusCode());
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        assertEquals(post.substring(0, post.length() - 1) + ",\"ID\":3}", response.body());
    }

    //postMovies с названием 101 символ вернет код 422
    @Test
    void postMovies_Line101_return422() throws IOException, InterruptedException {
        String post = "{\"title\":\"ааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааа\",\"description\":\"Приключение Ромарио\",\"genre\":\"Комедия\",\"duration\":110,\"director\":\"Паприкко\",\"year\":2026}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(post))
                .header("Content-Type","application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(422,response.statusCode());
        assertTrue(response.body().contains("Название не должно быть больше 100 символов"));
    }

    //postMovies с пустым название вернет код 422
    @Test
    void postMovies_Line0_return422() throws IOException, InterruptedException {
        String post = "{\"title\":\"\",\"description\":\"Приключение Ромарио\",\"genre\":\"Комедия\",\"duration\":110,\"director\":\"Паприкко\",\"year\":2026}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(post))
                .header("Content-Type","application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(422,response.statusCode());
        assertTrue(response.body().contains("Название не должно быть пустым"));
    }

    //postMovies с 1887 годом вернет код 422
    @Test
    void postMovies_year1887_return422() throws IOException, InterruptedException {
        String post = "{\"title\":\"Ромарио\",\"description\":\"Приключение Ромарио\",\"genre\":\"Комедия\",\"duration\":110,\"director\":\"Паприкко\",\"year\":1887}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(post))
                .header("Content-Type","application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(422,response.statusCode());
        assertTrue(response.body().contains("Год должен быть от 1888 до " + LocalDate.now().getYear() + 1));
    }

    //postMovies с годом 2027 вернет код 422
    @Test
    void postMovies_year2027_return422() throws IOException, InterruptedException {
        String post = "{\"title\":\"\",\"description\":\"Приключение Ромарио\",\"genre\":\"Комедия\",\"duration\":110,\"director\":\"Паприкко\",\"year\":2027}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(post))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type","application/json")
                .build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(422,response.statusCode());
        assertTrue(response.body().contains("Название не должно быть пустым"));
    }

    @Test
    void postMovies_Content_text_html_return_415() throws IOException, InterruptedException {
        String post = "{\"title\":\"\",\"description\":\"Приключение Ромарио\",\"genre\":\"Комедия\",\"duration\":110,\"director\":\"Паприкко\",\"year\":2027}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(post))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type","text/html")
                .build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());


        assertEquals(415,response.statusCode());


    }
}