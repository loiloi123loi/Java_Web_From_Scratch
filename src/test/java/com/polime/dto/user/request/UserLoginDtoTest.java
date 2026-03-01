package com.polime.dto.user.request;

import com.polime.exception.ValidationException;
import com.polime.test.BaseServiceTest;

public class UserLoginDtoTest extends BaseServiceTest {
    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    @Override
    public void runAllTests() {
        printHeader("UserLoginDto - validate()");
        runTest("validate_Success", this::validate_Success);
        runTest("validate_EmailRequired", this::validate_EmailRequired);
        runTest("validate_PasswordRequired", this::validate_PasswordRequired);
    }

    public void validate_Success() {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.validate();
    }

    public void validate_EmailRequired() {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("");
        dto.setPassword("password123");
        ValidationException ex = assertThrows(ValidationException.class, dto::validate);
        assertEquals("Email is required", ex.getMessage());
    }

    public void validate_PasswordRequired() {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("test@example.com");
        dto.setPassword("");
        ValidationException ex = assertThrows(ValidationException.class, dto::validate);
        assertEquals("Password is required", ex.getMessage());
    }
}
