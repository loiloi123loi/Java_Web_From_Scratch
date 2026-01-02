package com.polime.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.polime.dto.BaseResponseDto;
import com.polime.enums.EHttpStatus;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {
    private final HttpServer server;
    private final Map<String, HttpHandler> routes = new HashMap<>();
    private static final Gson gson = new Gson();

    public WebServer(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.setExecutor(null);

        this.server.createContext("/", exchange -> {
            byte[] response = gson.toJson(new BaseResponseDto<Object>("Route Not Found", "NOT_FOUND"))
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(404, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
    }

    public void addRoute(String path, HttpHandler handler) {
        routes.put(path, handler);
        server.createContext(path, exchange -> {
            try {
                handler.handle(exchange);
            } catch (Exception e) {
                System.err.println(String.format("!!! Unhandled Exception in Path [%s]: %s", path, e.getMessage()));
                e.printStackTrace();

                try {
                    sendJsonResponse(exchange, EHttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                            new BaseResponseDto<Object>("Internal server error", "INTERNAL_ERROR"));
                } catch (IOException ioException) {
                    exchange.close();
                }
            }
        });
    }

    public void start() {
        server.start();
        System.out.println("ServerAPI started on port " + server.getAddress().getPort());
        System.out.println("Registered Routes:");
        routes.keySet()
                .forEach(path -> System.out.println(" - http://localhost:" + server.getAddress().getPort() + path));
    }

    public static void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendJsonResponse(HttpExchange exchange, int statusCode, Object responseObject)
            throws IOException {
        String json = gson.toJson(responseObject);
        sendResponse(exchange, statusCode, json);
    }

    public static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
