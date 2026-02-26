package com.polime.core;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.polime.dto.BaseResponseDto;
import com.polime.enums.EHttpStatus;
import com.polime.enums.EResponseCode;
import com.polime.exception.DuplicateResourceException;
import com.polime.exception.InvalidCredentialsException;
import com.polime.exception.ResourceNotFoundException;
import com.polime.exception.UnauthorizedException;
import com.polime.exception.ValidationException;
import com.polime.utils.JwtUtils;
import com.polime.utils.TokenBlacklist;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.jsonwebtoken.Claims;

public abstract class BaseHandler implements HttpHandler {
    private final String basePath;
    private final Map<String, Map<String, RouteHandler>> routes = new HashMap<>();

    public BaseHandler(String basePath) {
        this.basePath = basePath;
        registerRoutes();
    }

    protected abstract void registerRoutes();

    protected void addRoute(String path, String method, RouteHandler handler) {
        routes.computeIfAbsent(path, k -> new HashMap<>()).put(method.toUpperCase(Locale.ROOT), handler);
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
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

        if (!RateLimiter.isAllowed(clientIp)) {
            WebServer.sendJsonResponse(exchange, 429, new BaseResponseDto<Object>(
                    "Too many requests. Please try again later.", EResponseCode.INTERNAL_ERROR));
            return;
        }

        try {
            String method = exchange.getRequestMethod().toUpperCase(Locale.ROOT);

            if (method.equals("OPTIONS")) {
                WebServer.sendResponse(exchange, 204, "");
                return;
            }

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
        } catch (ResourceNotFoundException e) {
            handleResourceNotFound(exchange, e);
        } catch (InvalidCredentialsException e) {
            handleInvalidCredentials(exchange, e);
        } catch (UnauthorizedException e) {
            handleUnauthorized(exchange, e);
        } catch (JsonSyntaxException e) {
            handleSyntaxError(exchange, e);
        } catch (SQLException e) {
            handleDatabaseError(exchange, e);
        } catch (Exception e) {
            handleInternalError(exchange, e);
        } finally {
            DatabaseManager.closeConnection();
        }
    }

    protected void handleNotFound(HttpExchange exchange) throws IOException {
        WebServer.sendJsonResponse(exchange, EHttpStatus.NOT_FOUND.getCode(),
                new BaseResponseDto<Object>("Resource Not Found", EResponseCode.NOT_FOUND));
    }

    protected void handleMethodNotAllowed(HttpExchange exchange) throws IOException {
        WebServer.sendJsonResponse(exchange, EHttpStatus.METHOD_NOT_ALLOWED.getCode(),
                new BaseResponseDto<Object>("Method Not Allowed", EResponseCode.METHOD_NOT_ALLOWED));
    }

    protected void handleValidationError(HttpExchange exchange, ValidationException e) throws IOException {
        WebServer.sendJsonResponse(exchange, EHttpStatus.BAD_REQUEST.getCode(),
                new BaseResponseDto<Object>(e.getMessage(), EResponseCode.VALIDATION_ERROR));
    }

    protected void handleDuplicateResource(HttpExchange exchange, DuplicateResourceException e) throws IOException {
        WebServer.sendJsonResponse(exchange, EHttpStatus.CONFLICT.getCode(),
                new BaseResponseDto<Object>(e.getMessage(), EResponseCode.DUPLICATE_RESOURCE));
    }

    protected void handleResourceNotFound(HttpExchange exchange, ResourceNotFoundException e) throws IOException {
        WebServer.sendJsonResponse(exchange, EHttpStatus.NOT_FOUND.getCode(),
                new BaseResponseDto<Object>(e.getMessage(), EResponseCode.RESOURCE_NOT_FOUND));
    }

    protected void handleInvalidCredentials(HttpExchange exchange, InvalidCredentialsException e) throws IOException {
        WebServer.sendJsonResponse(exchange, EHttpStatus.UNAUTHORIZED.getCode(),
                new BaseResponseDto<Object>(e.getMessage(), EResponseCode.INVALID_CREDENTIALS));
    }

    protected void handleUnauthorized(HttpExchange exchange, UnauthorizedException e) throws IOException {
        WebServer.sendJsonResponse(exchange, EHttpStatus.UNAUTHORIZED.getCode(),
                new BaseResponseDto<Object>(e.getMessage(), EResponseCode.UNAUTHORIZED));
    }

    protected void handleSyntaxError(HttpExchange exchange, JsonSyntaxException e) throws IOException {
        String message = e.getMessage();

        if (e.getCause() != null && e.getCause().getMessage() != null) {
            message = e.getCause().getMessage();
        }

        WebServer.sendJsonResponse(exchange, EHttpStatus.BAD_REQUEST.getCode(),
                new BaseResponseDto<Object>(message, EResponseCode.INVALID_JSON_SYNTAX));
    }

    protected void handleDatabaseError(HttpExchange exchange, SQLException e) throws IOException {
        WebServer.sendJsonResponse(exchange, EHttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                new BaseResponseDto<Object>("Database error occurred", EResponseCode.DATABASE_ERROR));
    }

    protected void handleInternalError(HttpExchange exchange, Exception e) throws IOException {
        WebServer.sendJsonResponse(exchange, EHttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                new BaseResponseDto<Object>("Internal server error", EResponseCode.INTERNAL_ERROR));
    }

    protected <T> T getBody(HttpExchange exchange, Gson gson, Class<T> clazz) throws IOException {
        String body = WebServer.readRequestBody(exchange);
        if (body == null || body.trim().isEmpty()) {
            throw new ValidationException("Request body is required");
        }

        T dto = gson.fromJson(body, clazz);
        if (dto == null) {
            throw new ValidationException("Request body is required");
        }

        return dto;
    }

    protected void authenticate(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new UnauthorizedException("Access token is required");
        }

        if (!authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Access token is required");
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new UnauthorizedException("Access token is required");
        }

        if (TokenBlacklist.isBlacklisted(token)) {
            throw new UnauthorizedException("Token has been revoked. Please login again.");
        }

        try {
            Claims claims = JwtUtils.decodeToken(token, JwtUtils.getAccessSecret());
            exchange.setAttribute(AppConstants.DECODED_AUTHORIZATION, claims);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid or expired access token");
        }
    }

    @FunctionalInterface
    public interface RouteHandler {
        void handle(HttpExchange exchange) throws IOException, SQLException;
    }
}
