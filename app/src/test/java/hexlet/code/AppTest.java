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
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
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
        url.setCreatedAt(new Timestamp(new Date(2024 / 9 / 21).getTime()));
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
            try (var response = client.post(NamedRoutes.urlChecksPath(url.getId()))) {
                assertThat(response.code()).isEqualTo(200);

                var check = UrlCheckRepository.find(url.getId()).orElseThrow();

                assertThat(check.getTitle()).isEqualTo("Тест");
                assertThat(check.getH1()).isEqualTo("Анализатор страниц");
                assertThat(check.getDescription()).isEqualTo("");
            } catch (final Exception th) {
                System.out.println(th.getMessage());
            }
        });
    }
}

//    private static MockWebServer mockServer;
//    private Javalin app;
//    private Map<String, Object> existingUrl;
//    private Map<String, Object> existingUrlCheck;
//    private HikariDataSource dataSource;
//
//    private static Path getFixturePath(String fileName) {
//        return Paths.get("src", "test", "resources", "fixtures", fileName)
//                .toAbsolutePath().normalize();
//    }
//
//    private static String readFixture(String fileName) throws IOException {
//        Path filePath = getFixturePath(fileName);
//        return Files.readString(filePath).trim();
//    }
//
//    private static String getDatabaseUrl() {
//        return System.getenv().getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:project");
//    }
//
//    @BeforeAll
//    public static void beforeAll() throws IOException {
//        mockServer = new MockWebServer();
//        MockResponse mockedResponse = new MockResponse()
//                .setBody(readFixture("index.html"));
//        mockServer.enqueue(mockedResponse);
//        mockServer.start();
//    }
//
//    @AfterAll
//    public static void afterAll() throws IOException {
//        mockServer.shutdown();
//    }
//
//    @BeforeEach
//    public void setUp() throws IOException, SQLException {
//        app = App.getApp();
//
//        var hikariConfig = new HikariConfig();
//        hikariConfig.setJdbcUrl(getDatabaseUrl());
//
//        dataSource = new HikariDataSource(hikariConfig);
//
//        var schema = AppTest.class.getClassLoader().getResource("schema.sql");
//        var file = new File(schema.getFile());
//        var sql = Files.lines(file.toPath())
//                .collect(Collectors.joining("\n"));
//
//        try (var connection = dataSource.getConnection();
//             var statement = connection.createStatement()) {
//            statement.execute(sql);
//        }
//
//        String url = "https://en.hexlet.io";
//
//        TestUtils.addUrl(dataSource, url);
//        existingUrl = TestUtils.getUrlByName(dataSource, url);
//
//        TestUtils.addUrlCheck(dataSource, (long) existingUrl.get("id"));
//        existingUrlCheck = TestUtils.getUrlCheck(dataSource, (long) existingUrl.get("id"));
//    }
//
//    @Nested
//    class RootTest {
//        @Test
//        void testIndex() {
//            JavalinTest.test(app, (server, client) -> {
//                assertThat(client.get("/").code()).isEqualTo(200);
//            });
//        }
//    }
//
//    @Nested
//    class UrlTest {
//
//        @Test
//        void testIndex() {
//            JavalinTest.test(app, (server, client) -> {
//                var response = client.get("/urls");
//                assertThat(response.code()).isEqualTo(200);
//                assertThat(response.body().string())
//                        .contains(existingUrl.get("name").toString())
//                        .contains(existingUrlCheck.get("status_code").toString());
//            });
//        }
//
//        @Test
//        void testShow() {
//            JavalinTest.test(app, (server, client) -> {
//                var response = client.get("/urls/" + existingUrl.get("id"));
//                assertThat(response.code()).isEqualTo(200);
//                assertThat(response.body().string())
//                        .contains(existingUrl.get("name").toString())
//                        .contains(existingUrlCheck.get("status_code").toString());
//            });
//        }
//
//        @Test
//        void testStore() {
//
//            String inputUrl = "https://ru.hexlet.io";
//
//            JavalinTest.test(app, (server, client) -> {
//                var requestBody = "url=" + inputUrl;
//                assertThat(client.post("/urls", requestBody).code()).isEqualTo(200);
//
//                var response = client.get("/urls");
//                assertThat(response.code()).isEqualTo(200);
//                assertThat(response.body().string())
//                        .contains(inputUrl);
//
//                var actualUrl = TestUtils.getUrlByName(dataSource, inputUrl);
//                assertThat(actualUrl).isNotNull();
//                assertThat(actualUrl.get("name").toString()).isEqualTo(inputUrl);
//            });
//        }
//    }
//
//    @Nested
//    class UrlCheckTest {
//
//        @Test
//        void testStore() {
//            String url = mockServer.url("/").toString().replaceAll("/$", "");
//
//            JavalinTest.test(app, (server, client) -> {
//                var requestBody = "url=" + url;
//                assertThat(client.post("/urls", requestBody).code()).isEqualTo(200);
//
//                var actualUrl = TestUtils.getUrlByName(dataSource, url);
//                assertThat(actualUrl).isNotNull();
//                System.out.println("\n!!!!!");
//                System.out.println(actualUrl);
//
//                System.out.println("\n");
//                assertThat(actualUrl.get("name").toString()).isEqualTo(url);
//
//                client.post("/urls/" + actualUrl.get("id") + "/checks");
//
//                assertThat(client.get("/urls/" + actualUrl.get("id")).code())
//                        .isEqualTo(200);
//
//                var actualCheck = TestUtils.getUrlCheck(dataSource, (long) actualUrl.get("id"));
//                assertThat(actualCheck).isNotNull();
//                assertThat(actualCheck.get("title")).isEqualTo("Test page");
//                assertThat(actualCheck.get("h1")).isEqualTo("Do not expect a miracle, miracles yourself!");
//                assertThat(actualCheck.get("description")).isEqualTo("statements of great people");
//            });
//        }
//    }
//}
