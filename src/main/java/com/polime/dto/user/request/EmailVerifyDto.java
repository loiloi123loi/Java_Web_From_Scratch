package com.polime.dto.user.request;

import com.polime.exception.ValidationException;

public class EmailVerifyDto {
    private String emailVerifyToken;

    public void validate() {
        if (emailVerifyToken == null || emailVerifyToken.trim().isEmpty()) {
            throw new ValidationException("Email verify token is required");
        }
    }

    public String getEmailVerifyToken() {
        return emailVerifyToken;
    }

    public void setEmailVerifyToken(String emailVerifyToken) {
        this.emailVerifyToken = emailVerifyToken;
    }
}
