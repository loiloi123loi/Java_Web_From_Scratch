package com.polime.core;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonSyntaxException;
import com.polime.dto.BaseResponseDto;
import com.polime.enums.EHttpStatus;
import com.polime.exception.DuplicateResourceException;
import com.polime.exception.ValidationException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class BaseHandler implements HttpHandler {
    private final String basePath;
    private final Map<String, Map<String, RouteHandler>> routes = new HashMap<>();

    public BaseHandler(String basePath) {
        this.basePath = basePath;
        registerRoutes();
    }

    protected abstract void registerRoutes();

    protected void addRoute(String path, String method, RouteHandler handler) {
        routes.computeIfAbsent(path, k -> new HashMap<>()).put(method.toUpperCase(), handler);
    }

    protected void get(String path, RouteHandler handler) {
        addRoute(path, "GET", handler);
    }

    protected void post(String path, RouteHandler handler) {
        addRoute(path, "POST", handler);
    }

    protected void put(String path, RouteHandler handler) {
        addRoute(path, "PUT", handler);
    }

    protected void patch(String path, RouteHandler handler) {
        addRoute(path, "PATCH", handler);
    }

    protected void delete(String path, RouteHandler handler) {
        addRoute(path, "DELETE", handler);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();

            if (path.startsWith(basePath)) {
                path = path.substring(basePath.length());
            }

            if (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, path.length() - 1);
            }

            if (path.isEmpty()) {
                path = "/";
            }

            Map<String, RouteHandler> methodHandlers = routes.get(path);
            if (methodHandlers != null) {
                String method = exchange.getRequestMethod().toUpperCase();
                RouteHandler handler = methodHandlers.get(method);

                if (handler != null) {
                    handler.handle(exchange);
                } else {
                    handleMethodNotAllowed(exchange);
                }
            } else {
                handleNotFound(exchange);
            }
        } catch (ValidationException e) {
            handleValidationError(exchange, e);
        } catch (DuplicateResourceException e) {
            handleDuplicateResource(exchange, e);
        } catch (JsonSyntaxException e) {
            handleSyntaxError(exchange, e);
        } catch (SQLException e) {
            handleDatabaseError(exchange, e);
        } catch (Exception e) {
            handleInternalError(exchange, e);
        }
    }

    protected void handleNotFound(HttpExchange exchange) throws IOException {
        WebServer.sendJsonResponse(exchange, EHttpStatus.NOT_FOUND.getCode(),
                new BaseResponseDto<Object>("Resource Not Found", "NOT_FOUND"));
    }

    protected void handleMethodNotAllowed(HttpExchange exchange) throws IOException {
        WebServer.sendJsonResponse(exchange, EHttpStatus.METHOD_NOT_ALLOWED.getCode(),
                new BaseResponseDto<Object>("Method Not Allowed", "METHOD_NOT_ALLOWED"));
    }

    protected void handleValidationError(HttpExchange exchange, ValidationException e) throws IOException {
        WebServer.sendJsonResponse(exchange, EHttpStatus.BAD_REQUEST.getCode(),
                new BaseResponseDto<Object>(e.getMessage(), "VALIDATION_ERROR"));
    }

    protected void handleDuplicateResource(HttpExchange exchange, DuplicateResourceException e) throws IOException {
        WebServer.sendJsonResponse(exchange, EHttpStatus.CONFLICT.getCode(),
                new BaseResponseDto<Object>(e.getMessage(), "DUPLICATE_RESOURCE"));
    }

    protected void handleSyntaxError(HttpExchange exchange, JsonSyntaxException e) throws IOException {
        String message = e.getMessage();

        if (e.getCause() != null && e.getCause().getMessage() != null) {
            message = e.getCause().getMessage();
        }

        WebServer.sendJsonResponse(exchange, EHttpStatus.BAD_REQUEST.getCode(),
                new BaseResponseDto<Object>(message, "INVALID_JSON_SYNTAX"));
    }

    protected void handleDatabaseError(HttpExchange exchange, SQLException e) throws IOException {
        WebServer.sendJsonResponse(exchange, EHttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                new BaseResponseDto<Object>("Database error occurred", "DATABASE_ERROR"));
    }

    protected void handleInternalError(HttpExchange exchange, Exception e) throws IOException {
        WebServer.sendJsonResponse(exchange, EHttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                new BaseResponseDto<Object>("Internal server error", "INTERNAL_ERROR"));
    }

    @FunctionalInterface
    public interface RouteHandler {
        void handle(HttpExchange exchange) throws IOException, SQLException;
    }
}
