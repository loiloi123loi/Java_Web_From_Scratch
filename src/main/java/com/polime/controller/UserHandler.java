package com.polime.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.polime.core.AppConstants;
import com.polime.core.BaseHandler;
import com.polime.core.WebServer;
import com.polime.dto.BaseResponseDto;
import com.polime.dto.user.request.UserLoginDto;
import com.polime.dto.user.request.UserLogoutDto;
import com.polime.dto.user.request.UserRegisterDto;
import com.polime.enums.EHttpStatus;
import com.polime.service.UserService;
import com.polime.utils.LocalDateAdapter;
import com.polime.utils.TokenBlacklist;
import com.sun.net.httpserver.HttpExchange;

import io.jsonwebtoken.Claims;

public class UserHandler extends BaseHandler {
    private final UserService userService;
    private final Gson gson;

    public UserHandler(String basePath, UserService userService) {
        super(basePath);
        this.userService = userService;
        this.gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
    }

    @Override
    protected void registerRoutes() {
        post("/register", this::handleRegister);
        post("/login", this::handleLogin);
        post("/logout", this::handleLogout);
    }

    private void handleRegister(HttpExchange exchange) throws IOException, SQLException {
        String body = WebServer.readRequestBody(exchange);
        UserRegisterDto dto = gson.fromJson(body, UserRegisterDto.class);

        dto.validate();

        WebServer.sendJsonResponse(exchange, EHttpStatus.CREATED.getCode(), userService.registerUser(dto));
    }

    private void handleLogin(HttpExchange exchange) throws IOException, SQLException {
        String body = WebServer.readRequestBody(exchange);
        UserLoginDto dto = gson.fromJson(body, UserLoginDto.class);

        dto.validate();

        WebServer.sendJsonResponse(exchange, EHttpStatus.OK.getCode(), userService.loginUser(dto));
    }

    private void handleLogout(HttpExchange exchange) throws IOException, SQLException {
        authenticate(exchange);
        Claims claims = (Claims) exchange.getAttribute(AppConstants.DECODED_AUTHORIZATION);
        Long userId = Long.parseLong(claims.getSubject());

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        String accessToken = authHeader.substring(7).trim();

        String body = WebServer.readRequestBody(exchange);
        UserLogoutDto dto = gson.fromJson(body, UserLogoutDto.class);

        dto.validate();

        BaseResponseDto<Object> response = userService.logoutUser(dto, userId);

        TokenBlacklist.add(accessToken, claims.getExpiration().getTime());

        WebServer.sendJsonResponse(exchange, EHttpStatus.OK.getCode(), response);
    }
}
