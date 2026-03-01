package com.polime.dto.user.request;

import java.time.LocalDate;

import com.polime.exception.ValidationException;
import com.polime.test.BaseServiceTest;

public class UserRegisterDtoTest extends BaseServiceTest {
    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    @Override
    public void runAllTests() {
        printHeader("UserRegisterDto - validate()");
        runTest("validate_Success", this::validate_Success);
        runTest("validate_EmailRequired", this::validate_EmailRequired);
        runTest("validate_EmailTooLong", this::validate_EmailTooLong);
        runTest("validate_InvalidEmailFormat", this::validate_InvalidEmailFormat);
        runTest("validate_PasswordRequired", this::validate_PasswordRequired);
        runTest("validate_PasswordTooShort", this::validate_PasswordTooShort);
        runTest("validate_PasswordsDoNotMatch", this::validate_PasswordsDoNotMatch);
        runTest("validate_NameRequired", this::validate_NameRequired);
        runTest("validate_DateOfBirthRequired", this::validate_DateOfBirthRequired);
        runTest("validate_Underage", this::validate_Underage);
        runTest("validate_NameTooLong", this::validate_NameTooLong);
    }

    public void validate_Success() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        dto.setName("Test User");
        dto.setDateOfBirth(LocalDate.now().minusYears(20));

        dto.validate();
    }

    public void validate_EmailRequired() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("");
        ValidationException ex = assertThrows(ValidationException.class, dto::validate);
        assertEquals("Email is required", ex.getMessage());
    }

    public void validate_EmailTooLong() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("a".repeat(250) + "@gmail.com");
        ValidationException ex = assertThrows(ValidationException.class, dto::validate);
        assertEquals("Email must be less than 255 characters", ex.getMessage());
    }

    public void validate_InvalidEmailFormat() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("invalid-email");
        ValidationException ex = assertThrows(ValidationException.class, dto::validate);
        assertEquals("Invalid email format", ex.getMessage());
    }

    public void validate_PasswordRequired() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("test@example.com");
        dto.setPassword(null);
        ValidationException ex = assertThrows(ValidationException.class, dto::validate);
        assertEquals("Password is required", ex.getMessage());
    }

    public void validate_PasswordTooShort() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("test@example.com");
        dto.setPassword("123");
        ValidationException ex = assertThrows(ValidationException.class, dto::validate);
        assertEquals("Password must be between 6 and 50 characters", ex.getMessage());
    }

    public void validate_PasswordsDoNotMatch() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setConfirmPassword("different");
        assertNotEquals(dto.getPassword(), dto.getConfirmPassword());
        ValidationException ex = assertThrows(ValidationException.class, dto::validate);
        assertEquals("Passwords do not match", ex.getMessage());
    }

    public void validate_NameRequired() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        dto.setName(null);
        ValidationException ex = assertThrows(ValidationException.class, dto::validate);
        assertEquals("Name is required", ex.getMessage());
    }

    public void validate_DateOfBirthRequired() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        dto.setName("Test");
        dto.setDateOfBirth(null);
        ValidationException ex = assertThrows(ValidationException.class, dto::validate);
        assertEquals("Date of birth is required", ex.getMessage());
    }

    public void validate_Underage() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        dto.setName("Teen");
        dto.setDateOfBirth(LocalDate.now().minusYears(10));
        ValidationException ex = assertThrows(ValidationException.class, dto::validate);
        assertEquals("You must be at least 13 years old", ex.getMessage());
    }

    public void validate_NameTooLong() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        dto.setName("a".repeat(256));
        ValidationException ex = assertThrows(ValidationException.class, dto::validate);
        assertEquals("Name must be less than 255 characters", ex.getMessage());
    }
}
