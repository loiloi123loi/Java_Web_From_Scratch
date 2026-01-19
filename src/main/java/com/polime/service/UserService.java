package com.polime.service;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import com.polime.core.DatabaseManager;
import com.polime.dto.BaseResponseDto;
import com.polime.dto.user.request.UserLoginDto;
import com.polime.dto.user.request.UserLogoutDto;
import com.polime.dto.user.request.UserRegisterDto;
import com.polime.dto.user.response.UserLoginResponseDto;
import com.polime.dto.user.response.UserRegisterResponseDto;
import com.polime.enums.EUserVerifyStatus;
import com.polime.exception.DuplicateResourceException;
import com.polime.exception.InvalidCredentialsException;
import com.polime.exception.ResourceNotFoundException;
import com.polime.exception.UnauthorizedException;
import com.polime.model.RefreshToken;
import com.polime.model.User;
import com.polime.repository.RefreshTokenRepository;
import com.polime.repository.UserRepository;
import com.polime.utils.JwtUtils;
import com.polime.utils.PasswordUtils;

import io.jsonwebtoken.Claims;

public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public BaseResponseDto<UserRegisterResponseDto> registerUser(UserRegisterDto dto) throws SQLException {
        try {
            DatabaseManager.beginTransaction();

            User existingUser = userRepository.findByEmail(dto.getEmail());
            if (existingUser != null) {
                throw new DuplicateResourceException("Email already exists");
            }

            User user = new User();
            user.setName(dto.getName());
            user.setEmail(dto.getEmail());
            user.setPassword(PasswordUtils.hashPassword(dto.getPassword()));
            user.setDateOfBirth(dto.getDateOfBirth());
            user.setCreatedAt(OffsetDateTime.now());
            user.setUpdatedAt(OffsetDateTime.now());
            user.setVerifyStatus(EUserVerifyStatus.Unverified);

            User savedUser = userRepository.save(user);
            Long userId = savedUser.getId();

            savedUser.setUsername("User" + userId);

            String emailVerifyToken = JwtUtils.signEmailVerifyToken(userId, EUserVerifyStatus.Unverified.name());
            savedUser.setEmailVerifyToken(emailVerifyToken);

            userRepository.update(savedUser);

            String accessToken = JwtUtils.signAccessToken(userId, EUserVerifyStatus.Unverified.name());
            String refreshTokenStr = JwtUtils.signRefreshToken(userId, EUserVerifyStatus.Unverified.name());

            Claims claims = JwtUtils.decodeToken(refreshTokenStr, JwtUtils.getRefreshSecret());

            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setUserId(userId);
            refreshToken.setToken(refreshTokenStr);
            refreshToken.setIat(OffsetDateTime.ofInstant(claims.getIssuedAt().toInstant(), ZoneId.systemDefault()));
            refreshToken.setExp(OffsetDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault()));

            refreshTokenRepository.save(refreshToken);

            DatabaseManager.commit();

            UserRegisterResponseDto result = new UserRegisterResponseDto(accessToken, refreshTokenStr);
            return new BaseResponseDto<>("User registered successfully", "SUCCESS", result);
        } catch (Exception e) {
            DatabaseManager.rollback();
            throw e;
        }
    }

    public BaseResponseDto<UserLoginResponseDto> loginUser(UserLoginDto dto) throws SQLException {
        try {
            DatabaseManager.beginTransaction();

            User user = userRepository.findByEmail(dto.getEmail());
            if (user == null) {
                throw new ResourceNotFoundException("User not found");
            }

            if (!PasswordUtils.verifyPassword(dto.getPassword(), user.getPassword())) {
                throw new InvalidCredentialsException("Invalid credentials");
            }

            String accessToken = JwtUtils.signAccessToken(user.getId(), user.getVerifyStatus().name());
            String refreshTokenStr = JwtUtils.signRefreshToken(user.getId(), user.getVerifyStatus().name());

            Claims claims = JwtUtils.decodeToken(refreshTokenStr, JwtUtils.getRefreshSecret());

            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setUserId(user.getId());
            refreshToken.setToken(refreshTokenStr);
            refreshToken.setIat(OffsetDateTime.ofInstant(claims.getIssuedAt().toInstant(), ZoneId.systemDefault()));
            refreshToken.setExp(OffsetDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault()));

            refreshTokenRepository.save(refreshToken);

            DatabaseManager.commit();

            UserLoginResponseDto result = new UserLoginResponseDto(accessToken, refreshTokenStr);
            return new BaseResponseDto<>("User logged in successfully", "SUCCESS", result);
        } catch (Exception e) {
            DatabaseManager.rollback();
            throw e;
        }
    }

    public BaseResponseDto<Object> logoutUser(UserLogoutDto dto, Long currentUserId) throws SQLException {
        try {
            Claims claims;
            try {
                claims = JwtUtils.decodeToken(dto.getRefreshToken(), JwtUtils.getRefreshSecret());
            } catch (Exception e) {
                throw new UnauthorizedException("Invalid or expired refresh token");
            }

            Long refreshTokenUserId = Long.parseLong(claims.getSubject());
            if (!refreshTokenUserId.equals(currentUserId)) {
                throw new UnauthorizedException("Unauthorized: Token mismatch");
            }

            if (!refreshTokenRepository.existsByToken(dto.getRefreshToken())) {
                throw new UnauthorizedException("Refresh token used or not exist");
            }

            DatabaseManager.beginTransaction();

            refreshTokenRepository.deleteByToken(dto.getRefreshToken());

            DatabaseManager.commit();

            return new BaseResponseDto<>("User logged out successfully", "SUCCESS");
        } catch (Exception e) {
            DatabaseManager.rollback();
            throw e;
        }
    }
}
