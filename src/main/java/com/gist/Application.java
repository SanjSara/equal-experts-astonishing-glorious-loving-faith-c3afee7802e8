package com.gist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;

public final class Application {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        HttpServer server = createServer(port);
        server.start();
        System.out.println("Gist API server started on port " + port);
    }

    public static HttpServer createServer(int port) throws IOException {
        var objectMapper = new ObjectMapper();
        var httpClient = HttpClient.newBuilder().build();
        var gistClient = new GitHubGistClient(httpClient, objectMapper);
        var handler = new GistHandler(gistClient, objectMapper);

        var server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", handler);
        return server;
    }
}
