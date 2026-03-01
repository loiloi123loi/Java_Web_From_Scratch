package com.polime.dto.user.request;

import com.polime.exception.ValidationException;

public class TokenRefreshDto {
    private String refreshToken;

    public TokenRefreshDto() {
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void validate() {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ValidationException("Refresh token is required");
        }
    }
}
