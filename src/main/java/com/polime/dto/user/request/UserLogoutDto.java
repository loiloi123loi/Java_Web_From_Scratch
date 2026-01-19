package com.polime.dto.user.request;

import com.polime.exception.ValidationException;

public class UserLogoutDto {
    private String refreshToken;

    public void validate() {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new ValidationException("Refresh token is required");
        }
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
