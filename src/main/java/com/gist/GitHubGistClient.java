package com.gist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gist.domain.Gist;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

public final class GitHubGistClient implements GistClient {

    private static final Logger LOG = Logger.getLogger(GitHubGistClient.class.getName());
    private static final String GITHUB_API = "https://api.github.com/users/%s/gists";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GitHubGistClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Gist> fetchPublicGists(String username) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(GITHUB_API.formatted(username)))
                .header("Accept", "application/vnd.github+json")
                .GET()
                .build();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                LOG.info("GitHub user not found: " + username);
                return List.of();
            }
            if (response.statusCode() != 200) {
                LOG.warning("GitHub API returned status %d for user: %s".formatted(response.statusCode(), username));
                throw new RuntimeException("GitHub API returned status %d".formatted(response.statusCode()));
            }

            JsonNode root = objectMapper.readTree(response.body());
            return StreamSupport.stream(root.spliterator(), false)
                    .map(this::toGist)
                    .toList();
        } catch (IOException | InterruptedException e) {
            LOG.log(Level.SEVERE, "Network error fetching gists for user: " + username, e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch gists for user: " + username, e);
        }
    }

    private Gist toGist(JsonNode node) {
        var files = StreamSupport.stream(node.path("files").spliterator(), false)
                .map(file -> file.path("filename").asText())
                .toList();

        return new Gist(
                node.path("id").asText(),
                node.path("description").asText(null),
                node.path("html_url").asText(),
                files
        );
    }
}
