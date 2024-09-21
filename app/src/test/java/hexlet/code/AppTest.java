package hexlet.code;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlsRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.javalin.testtools.JavalinTest;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;


public class AppTest {

    Javalin app;

    @BeforeEach
    public final void setUp() throws Exception {
        app = App.getApp();
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
        url.setCreatedAt(new Timestamp(new Date(2024/9/21).getTime()));
        UrlsRepository.save(url);

        JavalinTest.test(app, (server, client) -> {
            assertTrue(UrlsRepository.find(url.getId()).isPresent());

            var response = client.get(NamedRoutes.urlPath(url.getId()));

            assertThat(response.code()).isEqualTo(200);
            assertThat(Objects.requireNonNull(response.body()).string()).contains(input);
            assertEquals(UrlsRepository.find(url.getId()).orElseThrow().getName(), input);
            assertEquals(UrlsRepository.findByName(input).orElseThrow().getName(), input);
        });
    }

    @Test
    public void testCreateUrls() {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "https://www.google.com";
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

}
