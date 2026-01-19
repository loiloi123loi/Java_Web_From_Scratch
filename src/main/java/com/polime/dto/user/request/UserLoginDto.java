package com.polime.dto.user.request;

import com.polime.exception.ValidationException;

public class UserLoginDto {
    private String email;
    private String password;

    public void validate() {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password is required");
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
