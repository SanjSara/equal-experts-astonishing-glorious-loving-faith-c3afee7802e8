package com.gist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gist.domain.Gist;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Gist HTTP Handler")
class GistHandlerTest {

    private static final int TEST_PORT = 9876;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private HttpServer server;
    private final HttpClient client = HttpClient.newBuilder().build();

    private final GistClient stubClient = username -> List.of(
            new Gist("1", "First gist", "https://gist.github.com/1", List.of("file1.txt")),
            new Gist("2", "Second gist", "https://gist.github.com/2", List.of("a.py", "b.py"))
    );

    @BeforeEach
    void setUp() throws IOException {
        var handler = new GistHandler(stubClient, OBJECT_MAPPER);
        server = HttpServer.create(new InetSocketAddress(TEST_PORT), 0);
        server.createContext("/", handler);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    @DisplayName("returns 200 with gists JSON for valid username")
    void returnsGistsForValidUser() throws Exception {
        var response = get("/octocat");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type")).hasValue("application/json");
        assertThat(response.body()).contains("\"id\":\"1\"", "\"id\":\"2\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    @DisplayName("returns 400 when username is missing")
    void returnsBadRequestForMissingUsername(String path) throws Exception {
        var response = get(path);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("Username is required");
    }

    @Test
    @DisplayName("returns 502 when upstream GitHub call fails")
    void returnsGatewayErrorOnUpstreamFailure() throws Exception {
        server.stop(0);

        GistClient failingClient = username -> { throw new RuntimeException("network error"); };
        var handler = new GistHandler(failingClient, OBJECT_MAPPER);
        server = HttpServer.create(new InetSocketAddress(TEST_PORT), 0);
        server.createContext("/", handler);
        server.start();

        var response = get("/octocat");

        assertThat(response.statusCode()).isEqualTo(502);
        assertThat(response.body()).contains("Failed to fetch gists from GitHub");
    }

    private HttpResponse<String> get(String path) throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:%d%s".formatted(TEST_PORT, path)))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
