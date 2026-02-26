package com.polime.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.polime.dto.BaseResponseDto;
import com.polime.enums.EHttpStatus;
import com.polime.enums.EResponseCode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {
    private final HttpServer server;
    private final Map<String, HttpHandler> routes = new HashMap<>();
    private static final Gson gson = new Gson();
    private static Set<String> allowedOrigins = new HashSet<>(Arrays.asList("*"));

    public static void setAllowedOrigins(String origins) {
        if (origins == null || origins.isEmpty()) {
            allowedOrigins = new HashSet<>(Arrays.asList("*"));
            return;
        }
        allowedOrigins = Arrays.stream(origins.split(",")).map(String::trim).collect(Collectors.toSet());
    }

    public WebServer(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.setExecutor(null);

        this.server.createContext("/", exchange -> {
            sendJsonResponse(exchange, 404, new BaseResponseDto<Object>("Route Not Found", EResponseCode.NOT_FOUND));
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
                            new BaseResponseDto<Object>("Internal server error", EResponseCode.INTERNAL_ERROR));
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

        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("X-Frame-Options", "DENY");
        exchange.getResponseHeaders().set("X-XSS-Protection", "1; mode=block");
        exchange.getResponseHeaders().set("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        exchange.getResponseHeaders().set("Content-Security-Policy",
                "default-src 'self'; script-src 'self'; object-src 'none';");

        String requestOrigin = exchange.getRequestHeaders().getFirst("Origin");
        String originToSet = null;

        if (allowedOrigins.contains("*")) {
            originToSet = "*";
        } else if (requestOrigin != null && allowedOrigins.contains(requestOrigin)) {
            originToSet = requestOrigin;
        }

        if (originToSet != null) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", originToSet);
            if (!originToSet.equals("*")) {
                exchange.getResponseHeaders().set("Vary", "Origin");
            }
        }
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length == 0 ? -1 : bytes.length);

        if (bytes.length > 0) {
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    public static void sendJsonResponse(HttpExchange exchange, int statusCode, Object responseObject)
            throws IOException {
        String json = gson.toJson(responseObject);
        sendResponse(exchange, statusCode, json);
    }

    private static final long MAX_BODY_SIZE = 1024 * 1024;

    public static String readRequestBody(HttpExchange exchange) throws IOException {
        String contentLengthStr = exchange.getRequestHeaders().getFirst("Content-Length");
        if (contentLengthStr != null) {
            long contentLength = Long.parseLong(contentLengthStr);
            if (contentLength > MAX_BODY_SIZE) {
                throw new IOException("Request body too large. Maximum allowed is 1MB.");
            }
        }

        try (InputStream is = exchange.getRequestBody()) {
            byte[] allBytes = is.readNBytes((int) MAX_BODY_SIZE + 1);
            if (allBytes.length > MAX_BODY_SIZE) {
                throw new IOException("Request body too large. Maximum allowed is 1MB.");
            }
            return new String(allBytes, StandardCharsets.UTF_8);
        }
    }
}
