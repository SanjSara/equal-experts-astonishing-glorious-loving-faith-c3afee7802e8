package com.gist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public final class GistHandler implements HttpHandler {

    private final GistClient gistClient;
    private final ObjectMapper objectMapper;

    public GistHandler(GistClient gistClient, ObjectMapper objectMapper) {
        this.gistClient = gistClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String username = path.substring(1); // strip leading "/"

        if (username.isBlank()) {
            sendResponse(exchange, 400, "{\"error\":\"Username is required\"}");
            return;
        }

        try {
            var gists = gistClient.fetchPublicGists(username);
            String json = objectMapper.writeValueAsString(gists);
            sendResponse(exchange, 200, json);
        } catch (RuntimeException e) {
            sendResponse(exchange, 502, "{\"error\":\"Failed to fetch gists from GitHub\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = body.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
