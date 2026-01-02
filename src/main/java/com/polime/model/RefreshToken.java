package com.polime.model;

import java.time.OffsetDateTime;

public class RefreshToken {
    private Long id;
    private Long userId;
    private String token;
    private OffsetDateTime iat;
    private OffsetDateTime exp;

    public RefreshToken() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public OffsetDateTime getIat() {
        return iat;
    }

    public void setIat(OffsetDateTime iat) {
        this.iat = iat;
    }

    public OffsetDateTime getExp() {
        return exp;
    }

    public void setExp(OffsetDateTime exp) {
        this.exp = exp;
    }
}
