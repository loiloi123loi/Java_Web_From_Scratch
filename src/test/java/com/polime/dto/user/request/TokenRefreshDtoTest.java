package com.polime.dto.user.request;

import com.polime.exception.ValidationException;
import com.polime.test.BaseServiceTest;

public class TokenRefreshDtoTest extends BaseServiceTest {
    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    @Override
    public void runAllTests() {
        printHeader("TokenRefreshDto - validate()");
        runTest("validate_Success", this::validate_Success);
        runTest("validate_TokenRequired", this::validate_TokenRequired);
    }

    public void validate_Success() {
        TokenRefreshDto dto = new TokenRefreshDto();
        dto.setRefreshToken("some-refresh-token");
        dto.validate();
    }

    public void validate_TokenRequired() {
        TokenRefreshDto dto = new TokenRefreshDto();
        dto.setRefreshToken("");
        ValidationException ex = assertThrows(ValidationException.class, dto::validate);
        assertEquals("Refresh token is required", ex.getMessage());
    }
}
