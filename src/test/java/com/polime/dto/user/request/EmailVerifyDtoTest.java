package com.polime.dto.user.request;

import com.polime.exception.ValidationException;
import com.polime.test.BaseServiceTest;

public class EmailVerifyDtoTest extends BaseServiceTest {
    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    @Override
    public void runAllTests() {
        printHeader("EmailVerifyDto - validate()");
        runTest("validate_Success", this::validate_Success);
        runTest("validate_TokenRequired", this::validate_TokenRequired);
    }

    public void validate_Success() {
        EmailVerifyDto dto = new EmailVerifyDto();
        dto.setEmailVerifyToken("some-token");
        dto.validate();
    }

    public void validate_TokenRequired() {
        EmailVerifyDto dto = new EmailVerifyDto();
        dto.setEmailVerifyToken("");
        ValidationException ex = assertThrows(ValidationException.class, dto::validate);
        assertEquals("Email verify token is required", ex.getMessage());
    }
}
