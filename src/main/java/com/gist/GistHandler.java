package com.gist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class GistHandler implements HttpHandler {

    private static final Logger LOG = Logger.getLogger(GistHandler.class.getName());

    private final GistClient gistClient;
    private final ObjectMapper objectMapper;

    public GistHandler(GistClient gistClient, ObjectMapper objectMapper) {
        this.gistClient = gistClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        long startTime = System.currentTimeMillis();

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
            LOG.info("GET /%s completed in %dms [200]".formatted(username, elapsed(startTime)));
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "GET /%s failed in %dms [502]".formatted(username, elapsed(startTime)), e);
            sendResponse(exchange, 502, "{\"error\":\"Failed to fetch gists from GitHub\"}");
        }
    }

    private long elapsed(long startTime) {
        return System.currentTimeMillis() - startTime;
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
