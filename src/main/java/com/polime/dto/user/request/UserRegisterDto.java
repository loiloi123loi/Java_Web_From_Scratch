package com.polime.dto.user.request;

import java.time.LocalDate;

import com.polime.exception.ValidationException;

public class UserRegisterDto {
    private String email;
    private String password;
    private String confirmPassword;
    private String name;
    private LocalDate dateOfBirth;

    public void validate() {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        email = email.trim();
        if (email.length() > 255) {
            throw new ValidationException("Email must be less than 255 characters");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("Invalid email format");
        }

        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password is required");
        }
        password = password.trim();
        if (password.length() < 6 || password.length() > 50) {
            throw new ValidationException("Password must be between 6 and 50 characters");
        }

        if (confirmPassword == null || !confirmPassword.trim().equals(password)) {
            throw new ValidationException("Passwords do not match");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Name is required");
        }
        name = name.trim();
        if (name.length() > 255) {
            throw new ValidationException("Name must be less than 255 characters");
        }

        if (dateOfBirth == null) {
            throw new ValidationException("Date of birth is required");
        }
        if (dateOfBirth.isAfter(LocalDate.now().minusYears(13))) {
            throw new ValidationException("You must be at least 13 years old");
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
