package com.gist;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Gist API integration test")
@Tag("integration")
class GistApiIntegrationTest {

    private static final int TEST_PORT = 9877;
    private static HttpServer server;
    private static final HttpClient client = HttpClient.newBuilder().build();

    @BeforeAll
    static void startServer() throws Exception {
        server = Application.createServer(TEST_PORT);
        server.start();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    @DisplayName("GET /octocat returns 200 with gists from GitHub")
    void returnsGistsForOctocat() throws Exception {
        var response = get("/octocat");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type")).hasValue("application/json");
        assertThat(response.body())
                .contains("\"id\"")
                .contains("\"htmlUrl\"")
                .contains("\"files\"");
    }

    @Test
    @DisplayName("GET /nonexistent-user-xyz-99999 returns 200 with empty list")
    void returnsEmptyListForUnknownUser() throws Exception {
        var response = get("/nonexistent-user-xyz-99999");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("[]");
    }

    private HttpResponse<String> get(String path) throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:%d%s".formatted(TEST_PORT, path)))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
