package com.polime.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.polime.enums.EUserVerifyStatus;

public class User {
    private Long id;
    private String name;
    private String username;
    private String email;
    private LocalDate dateOfBirth;
    private String password;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private EUserVerifyStatus verifyStatus;
    private String emailVerifyToken;
    private String forgotPasswordToken;

    public User() {
    }

    public User(Long id, String name, String username, String email, LocalDate dateOfBirth, String password,
            OffsetDateTime createdAt, OffsetDateTime updatedAt, String emailVerifyToken, String forgotPasswordToken,
            EUserVerifyStatus verifyStatus) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.password = password;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.emailVerifyToken = emailVerifyToken;
        this.forgotPasswordToken = forgotPasswordToken;
        this.verifyStatus = verifyStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public EUserVerifyStatus getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(EUserVerifyStatus verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    public String getEmailVerifyToken() {
        return emailVerifyToken;
    }

    public void setEmailVerifyToken(String emailVerifyToken) {
        this.emailVerifyToken = emailVerifyToken;
    }

    public String getForgotPasswordToken() {
        return forgotPasswordToken;
    }

    public void setForgotPasswordToken(String forgotPasswordToken) {
        this.forgotPasswordToken = forgotPasswordToken;
    }
}
