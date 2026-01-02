package com.polime.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.polime.core.BaseHandler;
import com.polime.core.WebServer;
import com.polime.dto.user.request.UserRegisterDto;
import com.polime.enums.EHttpStatus;
import com.polime.service.UserService;
import com.polime.utils.LocalDateAdapter;
import com.sun.net.httpserver.HttpExchange;

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
    }

    private void handleRegister(HttpExchange exchange) throws IOException, SQLException {
        String body = WebServer.readRequestBody(exchange);
        UserRegisterDto dto = gson.fromJson(body, UserRegisterDto.class);

        dto.validate();

        WebServer.sendJsonResponse(exchange, EHttpStatus.CREATED.getCode(), userService.registerUser(dto));
    }
}
