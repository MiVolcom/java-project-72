package hexlet.code;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import hexlet.code.repository.UrlCheckRepository;
import io.javalin.Javalin;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static hexlet.code.App.readResourceFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.javalin.testtools.JavalinTest;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;


public class AppTest {

    private static Javalin app;
    private static MockWebServer mockServer;
    private static String baseUrl;

    @BeforeAll
    public static void beforeAll() throws IOException {
        mockServer = new MockWebServer();
        baseUrl = mockServer.url("/").toString();
        MockResponse mockResponse = new MockResponse().setBody(readResourceFile("fixtures/test.html"));
        mockServer.enqueue(mockResponse);
    }

    @BeforeEach
    public final void setUp() throws IOException, SQLException {
        app = App.getApp();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        mockServer.shutdown();
    }

    @Test
    public void testBasePage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/");
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("Анализатор страниц");
        });
    }

    @Test
    public void testUrlsPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlsPath());
            assertThat(response.code()).isEqualTo(200);
        });
    }

    @Test
    public void testUrlPage() throws SQLException {
        String input = "https://www.google.com";
        var url = new Url(input);
        LocalDateTime dateTime = LocalDateTime.of(2024, 10, 21, 0, 0);
        Instant instant = dateTime.toInstant(ZoneOffset.UTC);
        url.setCreatedAt(instant);
        UrlRepository.save(url);

        JavalinTest.test(app, (server, client) -> {
            assertTrue(UrlRepository.find(url.getId()).isPresent());

            var response = client.get(NamedRoutes.urlPath(url.getId()));

            assertThat(response.code()).isEqualTo(200);
            assertThat(Objects.requireNonNull(response.body()).string()).contains(input);
            assertEquals(UrlRepository.find(url.getId()).orElseThrow().getName(), input);
            assertEquals(UrlRepository.findByName(input).orElseThrow().getName(), input);
        });
    }

    @Test
    public void testCreateUrls() {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=https://www.google.com";
            var response = client.post(NamedRoutes.urlsPath(), requestBody);
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("https://www.google.com");
        });
    }

    @Test
    void testUrlNotFound() {
        var id = 999999;
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlPath((long) id));
            assertThat(response.code()).isEqualTo(404);
        });
    }

    @Test
    void testUrlCheck() throws SQLException {

        Url url = new Url(baseUrl);
        UrlRepository.save(url);

        JavalinTest.test(app, (server, client) -> {
            var response = client.post(NamedRoutes.urlChecksPath(url.getId()));
            assertThat(response.code()).isEqualTo(200);
            var check = UrlCheckRepository.find(url.getId()).orElseThrow();

            assertThat(check.getTitle()).isEqualTo("Тест");
            assertThat(check.getH1()).isEqualTo("Анализатор страниц");
            assertThat(check.getDescription()).isEqualTo("");
        });
    }
}
