package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.ErrorResponse;
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
    protected static final String BASE = "http://localhost:8080"; // !!! добавьте базовую часть URL
    protected static MoviesServer server;
    protected static HttpClient client;
    protected static Gson gson = new Gson();
    protected static MoviesStore movies;

    protected static final Movie movie1;
    protected static final Movie movie2;


    static {
        try {
            movie1 = new Movie("Пираты", "Пираты в море", "Драма", 87, "Робертто", 2000);
            movie2 = new Movie("Вторжение", "Космические захватчики", "Экшен", 120, "Дуррито", 1999);
        } catch (ErrorResponse e) {
            throw new RuntimeException(e);
        }
    }

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
    void beforeEach() throws ErrorResponse {
        movies.clear(); // очищаем хранилище
        Movie movie1 = new Movie("Пираты", "Пираты в море", "Драма", 87, "Робертто", 2000);
        Movie movie2 = new Movie("Вторжение", "Космические захватчики", "Экшен", 120, "Дуррито", 1999);

        movies.addMovie(movie1);
        movies.addMovie(movie2);
    }

    //getAllMovies с пустым списком вернет код 200
    @Test
    void getAllMovies_whenEmpty_returnsEmptyArray_code_200() throws Exception {
        MoviesStore emptyStore = new MoviesStore();
        MoviesServer emptyServer = new MoviesServer(emptyStore, 8081);
        emptyServer.start();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/movies")) // !!! Добавьте правильный URI
                .GET()
                .header("Content-Type", "application/json")
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

    //getAllMovies вернет заполненный список и код 200
    @Test
    void getAllMovies_returnArray_code_200() {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
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

    //getMovieID с id1 вернет элемент и код 200
    @Test
    void getMovieID_ID1_return_code_200() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .header("Content-Type", "application/json")
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(200, response.statusCode());
        assertEquals(movie1, gson.fromJson(response.body(), Movie.class));

    }

    //getMovieID с несуществующим id10 вернет код 404
    @Test
    void getMovieID_ID0_return_cdoe_404() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/10"))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Фильм не найден"));
    }

    //getMovieID с некорректным id вернет 400
    @Test
    void getMovieID_IDabc_return_code_400() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/abc"))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Некорректный ID"));
    }

    //getMoviesFilterYear вернет список фильмов 2000 годов
    @Test
    void getMoviesFilterYear_2005_return_code_200() throws IOException, InterruptedException, ErrorResponse {

        Movie pikol = new Movie("Пикол", "История Пикола", "Экшен", 1400, "Дуррито", 2000);
        Movie vudi = new Movie("Вуди", "Магическое приключение", "фэнтези", 80, "Дуррито", 2000);
        Movie vtorzhenie2 = new Movie("Вторжение 2", "Космические захватчики", "Экшен", 160, "Дуррито", 2007);

        movies.addMovie(pikol);
        movies.addMovie(vudi);
        movies.addMovie(vtorzhenie2);

        List<Movie> expected = List.of(movie1, pikol, vudi);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2000"))
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        assertEquals(200, response.statusCode());
        assertEquals(expected, gson.fromJson(response.body(), new ListOfMoviesTypeToken().getType()));

    }

    //getMoviesFilterYear вернет пустой список с кодом 200
    @Test
    void getMoviesFilterYear_2010_return_code_200() throws IOException, InterruptedException {

        MoviesStore emptyStore = new MoviesStore();
        MoviesServer emptyServer = new MoviesServer(emptyStore, 8082);
        emptyServer.start();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2010"))
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        assertEquals(200, response.statusCode());

        assertTrue(response.body().startsWith("[") && response.body().endsWith("]") && response.body().length() == 2,
                "Ожидается JSON-массив");
    }

    @Test
    void getMoviesFilterYear_abc_return_code_400() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=abc"))
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        assertEquals(400, response.statusCode());

    }

    //addNewMovie успешное добавление поста с кодом 201
    @Test
    void addNewMovie_return_code_201() throws IOException, InterruptedException {
        String post = "{\"title\":\"Ромарио\",\"description\":\"Приключение Ромарио\",\"genre\":\"Комедия\",\"duration\":110,\"director\":\"Паприкко\",\"year\":2026}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(post))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals(201, response.statusCode());
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        assertEquals(post.substring(0, post.length() - 1) + ",\"id\":3}", response.body());
    }

    //addNewMovie с названием 101 символ вернет код 422
    @Test
    void addNewMovie_Line101_return_code_422() throws IOException, InterruptedException {
        String post = "{\"title\":\"ааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааа\",\"description\":\"Приключение Ромарио\",\"genre\":\"Комедия\",\"duration\":110,\"director\":\"Паприкко\",\"year\":2026}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(post))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(422, response.statusCode());
        assertTrue(response.body().contains("Название не должно быть больше 100 символов"));
    }

    //addNewMovie с пустым название вернет код 422
    @Test
    void addNewMovie_Line0_return_code_422() throws IOException, InterruptedException {
        String post = "{\"title\":\"\",\"description\":\"Приключение Ромарио\",\"genre\":\"Комедия\",\"duration\":110,\"director\":\"Паприкко\",\"year\":2026}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(post))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(422, response.statusCode());
        assertTrue(response.body().contains("Название не должно быть пустым"));
    }

    //addNewMovie с 1887 годом вернет код 422
    @Test
    void addNewMovie_year1887_return_code_422() throws IOException, InterruptedException {
        String post = "{\"title\":\"Ромарио\",\"description\":\"Приключение Ромарио\",\"genre\":\"Комедия\",\"duration\":110,\"director\":\"Паприкко\",\"year\":1887}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(post))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(422, response.statusCode());
        assertTrue(response.body().contains("Год должен быть от 1888 до " + LocalDate.now().getYear() + 1));
    }

    //addNewMovie с годом 2027 вернет код 422
    @Test
    void addNewMovie_year2027_return_code_422() throws IOException, InterruptedException {
        String post = "{\"title\":\"\",\"description\":\"Приключение Ромарио\",\"genre\":\"Комедия\",\"duration\":110,\"director\":\"Паприкко\",\"year\":2027}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(post))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(422, response.statusCode());
        assertTrue(response.body().contains("Название не должно быть пустым"));
    }


    //addNewMovie с неправильным Content-Type вернет код 415
    @Test
    void addNewMovie_Content_text_html_return_code_415() throws IOException, InterruptedException {
        String post = "{\"title\":\"\",\"description\":\"Приключение Ромарио\",\"genre\":\"Комедия\",\"duration\":110,\"director\":\"Паприкко\",\"year\":2027}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(post))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "text/html")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        assertEquals(415, response.statusCode());


    }

    //addNewMovies в некорректным JSON вернет код 400
    @Test
    void addNewMovies_invalidJson_return_code_400() throws IOException, InterruptedException {
        String invalidJson = "это не json {{{";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals(400, response.statusCode());
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue);
        assertTrue(response.body().contains("Неправильный JSON"));
    }

    //deleteMovie с корректным ID2 удалит элемент и вернет код 204
    @Test
    void deleteMovies_ID2_return_code_204() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/2"))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(204, response.statusCode());
    }

    //deleteMovie с id5 которого нет вернет код 404
    @Test
    void deleteMovies_ID5_return_code_404() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/5"))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(404, response.statusCode());
    }

    @Test
    void deleteMovies_IDabc_return_code_400() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/abc"))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                response.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Некорректный ID"));
    }

}