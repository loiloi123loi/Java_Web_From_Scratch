package com.polime.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.google.gson.Gson;
import com.polime.core.AppConstants;
import com.polime.core.BaseHandler;
import com.polime.core.WebServer;
import com.polime.dto.post.request.PostCreateDto;
import com.polime.enums.EHttpStatus;
import com.polime.service.PostService;
import com.sun.net.httpserver.HttpExchange;

import io.jsonwebtoken.Claims;

public class PostHandler extends BaseHandler {
    private final PostService postService;
    private final Gson gson;

    public PostHandler(String basePath, PostService postService) {
        super(basePath);
        this.postService = postService;
        this.gson = new Gson();
    }

    @Override
    protected void registerRoutes() {
        post("/", this::handleCreatePost);
    }

    private void handleCreatePost(HttpExchange exchange) throws IOException, SQLException {
        authenticate(exchange);
        Claims claims = (Claims) exchange.getAttribute(AppConstants.DECODED_AUTHORIZATION);
        Long userId = Long.parseLong(claims.getSubject());

        PostCreateDto dto = getBody(exchange, gson, PostCreateDto.class);
        dto.validate();

        WebServer.sendJsonResponse(exchange, EHttpStatus.CREATED.getCode(), postService.createPost(userId, dto));
    }
}
