package com.polime;

import com.polime.controller.PostHandler;
import com.polime.controller.UserHandler;
import com.polime.core.AppConfig;
import com.polime.core.DatabaseManager;
import com.polime.core.WebServer;
import com.polime.repository.PostRepository;
import com.polime.repository.RefreshTokenRepository;
import com.polime.repository.UserRepository;
import com.polime.service.PostService;
import com.polime.service.UserService;
import com.polime.utils.JwtUtils;

public class SocialApp {
    private static final String BASE_PATH = "/api/v1";
    private static final String USER_API_PATH = BASE_PATH + "/users";
    private static final String POST_API_PATH = BASE_PATH + "/posts";

    public static void main(String[] args) {
        try {
            System.out.println("Starting Social Network API (Manual Mode)...");

            AppConfig config = new AppConfig();

            DatabaseManager.init(config.getProperty("db.url"), config.getProperty("db.username"),
                    config.getProperty("db.password"));
            System.out.println("-> Database Manager Initialized");

            JwtUtils.init(config.getProperty("jwt.access_token_secret"),
                    config.getLongProperty("jwt.access_token_expires_in", 900000L),
                    config.getProperty("jwt.refresh_token_secret"),
                    config.getLongProperty("jwt.refresh_token_expires_in", 2592000000L),
                    config.getProperty("jwt.email_verify_token_secret"),
                    config.getLongProperty("jwt.email_verify_token_expires_in", 604800000L));

            int port = config.getIntProperty("server.port", 8080);
            WebServer.setAllowedOrigins(config.getProperty("server.cors.allow_origin", "http://localhost:3000"));

            UserRepository userRepository = new UserRepository();
            userRepository.initTable();

            RefreshTokenRepository refreshTokenRepository = new RefreshTokenRepository();
            refreshTokenRepository.initTable();

            PostRepository postRepository = new PostRepository();
            postRepository.initTable();

            DatabaseManager.closeConnection();

            UserService userService = new UserService(userRepository, refreshTokenRepository);
            UserHandler userHandler = new UserHandler(USER_API_PATH, userService);

            PostService postService = new PostService(postRepository);
            PostHandler postHandler = new PostHandler(POST_API_PATH, postService);

            WebServer server = new WebServer(port);
            server.addRoute(USER_API_PATH, userHandler);
            server.addRoute(POST_API_PATH, postHandler);

            server.start();
            System.out.println("-> Ready to accept requests at http://localhost:" + port);
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeConnection();
        }
    }
}
