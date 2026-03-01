package com.polime.service;

import java.time.LocalDate;

import com.polime.core.DatabaseManager;
import com.polime.dto.BaseResponseDto;
import com.polime.dto.user.request.EmailVerifyDto;
import com.polime.dto.user.request.TokenRefreshDto;
import com.polime.dto.user.request.UserLoginDto;
import com.polime.dto.user.request.UserLogoutDto;
import com.polime.dto.user.request.UserRegisterDto;
import com.polime.dto.user.response.TokenRefreshResponseDto;
import com.polime.dto.user.response.UserLoginResponseDto;
import com.polime.dto.user.response.UserRegisterResponseDto;
import com.polime.enums.EResponseCode;
import com.polime.enums.EUserVerifyStatus;
import com.polime.exception.DuplicateResourceException;
import com.polime.exception.InvalidCredentialsException;
import com.polime.exception.ResourceNotFoundException;
import com.polime.exception.UnauthorizedException;
import com.polime.exception.ValidationException;
import com.polime.model.RefreshToken;
import com.polime.model.User;
import com.polime.repository.RefreshTokenRepository;
import com.polime.repository.UserRepository;
import com.polime.test.BaseServiceTest;
import com.polime.utils.JwtUtils;
import com.polime.utils.TokenBlacklist;

public class UserServiceTest extends BaseServiceTest {
    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private UserService userService;

    @Override
    protected void setUp() throws Exception {
        userRepository = new UserRepository();
        refreshTokenRepository = new RefreshTokenRepository();
        userService = new UserService(userRepository, refreshTokenRepository);

        userRepository.initTable();
        refreshTokenRepository.initTable();

        clearDatabase();
    }

    @Override
    protected void tearDown() throws Exception {
        clearDatabase();
    }

    @Override
    public void runAllTests() {
        printHeader("UserService - registerUser()");
        runTest("registerUser_Success", this::registerUser_Success);
        runTest("registerUser_EmailAlreadyExists", this::registerUser_EmailAlreadyExists);
        runTest("registerUser_ShouldSetCorrectUsername", this::registerUser_ShouldSetCorrectUsername);
        runTest("registerUser_ShouldGenerateEmailVerifyToken", this::registerUser_ShouldGenerateEmailVerifyToken);
        runTest("registerUser_ShouldHashPassword", this::registerUser_ShouldHashPassword);
        runTest("registerUser_TokensShouldBeDifferent", this::registerUser_TokensShouldBeDifferent);
        runTest("registerUser_ShouldSaveRefreshToken", this::registerUser_ShouldSaveRefreshToken);
        runTest("registerUser_MultipleUsersWithDifferentEmails", this::registerUser_MultipleUsersWithDifferentEmails);
        runTest("registerUser_ShouldSaveCorrectUserInfo", this::registerUser_ShouldSaveCorrectUserInfo);

        printHeader("UserService - loginUser()");
        runTest("loginUser_Success", this::loginUser_Success);
        runTest("loginUser_UserNotFound", this::loginUser_UserNotFound);
        runTest("loginUser_BannedUser", this::loginUser_BannedUser);
        runTest("loginUser_InvalidPassword", this::loginUser_InvalidPassword);

        printHeader("UserService - verifyEmail()");
        runTest("verifyEmail_Success", this::verifyEmail_Success);
        runTest("verifyEmail_UserNotFound", this::verifyEmail_UserNotFound);
        runTest("verifyEmail_BannedUser", this::verifyEmail_BannedUser);
        runTest("verifyEmail_AlreadyVerified", this::verifyEmail_AlreadyVerified);
        runTest("verifyEmail_InvalidToken", this::verifyEmail_InvalidToken);
        runTest("verifyEmail_TokenUserIdMismatch", this::verifyEmail_TokenUserIdMismatch);

        printHeader("UserService - logoutUser()");
        runTest("logoutUser_Success", this::logoutUser_Success);
        runTest("logoutUser_InvalidRefreshToken", this::logoutUser_InvalidRefreshToken);
        runTest("logoutUser_TokenMismatch", this::logoutUser_TokenMismatch);
        runTest("logoutUser_TokenNotExist", this::logoutUser_TokenNotExist);

        printHeader("UserService - refreshToken()");
        runTest("refreshToken_Success", this::refreshToken_Success);
        runTest("refreshToken_InvalidToken", this::refreshToken_InvalidToken);
        runTest("refreshToken_TokenNotExist", this::refreshToken_TokenNotExist);
        runTest("refreshToken_UserNotFound", this::refreshToken_UserNotFound);
    }

    public void clearDatabase() throws Exception {
        var conn = DatabaseManager.getConnection();
        try (var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM refresh_tokens");
            stmt.execute("DELETE FROM users");
        }
    }

    public void registerUser_Success() throws Throwable {
        UserRegisterDto dto = createValidRegisterDto();

        BaseResponseDto<UserRegisterResponseDto> response = userService.registerUser(dto);

        assertNotNull(response);
        assertEquals(EResponseCode.SUCCESS, response.getCode());
        assertEquals("User registered successfully", response.getMessage());
        assertNotNull(response.getResult());
        assertNotNull(response.getResult().getAccessToken());
        assertNotNull(response.getResult().getRefreshToken());
    }

    public void registerUser_EmailAlreadyExists() throws Throwable {
        UserRegisterDto firstDto = createValidRegisterDto();
        userService.registerUser(firstDto);

        UserRegisterDto duplicateDto = createValidRegisterDto();
        duplicateDto.setName("Another User");

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> userService.registerUser(duplicateDto));
        assertEquals("Email already exists", exception.getMessage());
    }

    public void registerUser_ShouldSetCorrectUsername() throws Throwable {
        UserRegisterDto dto = createValidRegisterDto();

        userService.registerUser(dto);

        User savedUser = userRepository.findByEmail(dto.getEmail());
        assertNotNull(savedUser);
        assertTrue(savedUser.getUsername().startsWith("User"));
        assertEquals("User" + savedUser.getId(), savedUser.getUsername());
    }

    public void registerUser_ShouldGenerateEmailVerifyToken() throws Throwable {
        UserRegisterDto dto = createValidRegisterDto();

        userService.registerUser(dto);

        User savedUser = userRepository.findByEmail(dto.getEmail());
        assertNotNull(savedUser);
        assertNotNull(savedUser.getEmailVerifyToken());
        assertTrue(savedUser.getEmailVerifyToken().length() > 0);
    }

    public void registerUser_ShouldHashPassword() throws Throwable {
        UserRegisterDto dto = createValidRegisterDto();
        String plainPassword = dto.getPassword();

        userService.registerUser(dto);

        User savedUser = userRepository.findByEmail(dto.getEmail());
        assertNotNull(savedUser);
        assertTrue(!plainPassword.equals(savedUser.getPassword()));
        assertTrue(savedUser.getPassword().startsWith("$2"));
    }

    public void registerUser_TokensShouldBeDifferent() throws Throwable {
        UserRegisterDto dto = createValidRegisterDto();

        BaseResponseDto<UserRegisterResponseDto> response = userService.registerUser(dto);

        assertNotNull(response.getResult());
        String accessToken = response.getResult().getAccessToken();
        String refreshToken = response.getResult().getRefreshToken();

        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        assertTrue(!accessToken.equals(refreshToken));
    }

    public void registerUser_ShouldSaveRefreshToken() throws Throwable {
        UserRegisterDto dto = createValidRegisterDto();

        BaseResponseDto<UserRegisterResponseDto> response = userService.registerUser(dto);

        String refreshToken = response.getResult().getRefreshToken();
        boolean exists = refreshTokenRepository.existsByToken(refreshToken);
        assertTrue(exists);
    }

    public void registerUser_MultipleUsersWithDifferentEmails() throws Throwable {
        UserRegisterDto dto1 = createValidRegisterDto();
        dto1.setEmail("user1@example.com");

        UserRegisterDto dto2 = createValidRegisterDto();
        dto2.setEmail("user2@example.com");

        BaseResponseDto<UserRegisterResponseDto> response1 = userService.registerUser(dto1);
        BaseResponseDto<UserRegisterResponseDto> response2 = userService.registerUser(dto2);

        assertEquals(EResponseCode.SUCCESS, response1.getCode());
        assertEquals(EResponseCode.SUCCESS, response2.getCode());

        User user1 = userRepository.findByEmail("user1@example.com");
        User user2 = userRepository.findByEmail("user2@example.com");

        assertNotNull(user1);
        assertNotNull(user2);
        assertTrue(!user1.getId().equals(user2.getId()));
    }

    public void registerUser_ShouldSaveCorrectUserInfo() throws Throwable {
        UserRegisterDto dto = createValidRegisterDto();
        dto.setName("John Doe");
        dto.setEmail("john.doe@example.com");
        dto.setDateOfBirth(LocalDate.of(1990, 5, 20));

        userService.registerUser(dto);

        User savedUser = userRepository.findByEmail("john.doe@example.com");
        assertNotNull(savedUser);
        assertEquals("John Doe", savedUser.getName());
        assertEquals("john.doe@example.com", savedUser.getEmail());
        assertEquals(LocalDate.of(1990, 5, 20), savedUser.getDateOfBirth());
    }

    public void loginUser_Success() throws Throwable {
        UserRegisterDto reg = createValidRegisterDto();
        userService.registerUser(reg);

        UserLoginDto login = new UserLoginDto();
        login.setEmail(reg.getEmail());
        login.setPassword(reg.getPassword());

        BaseResponseDto<UserLoginResponseDto> res = userService.loginUser(login);
        assertEquals(EResponseCode.SUCCESS, res.getCode());
        assertNotNull(res.getResult().getAccessToken());
    }

    public void loginUser_UserNotFound() throws Throwable {
        UserLoginDto login = new UserLoginDto();
        login.setEmail("notfound@example.com");
        login.setPassword("any");

        assertThrows(ResourceNotFoundException.class, () -> userService.loginUser(login));
    }

    public void loginUser_BannedUser() throws Throwable {
        UserRegisterDto reg = createValidRegisterDto();
        userService.registerUser(reg);
        User user = userRepository.findByEmail(reg.getEmail());
        user.setVerifyStatus(EUserVerifyStatus.Banned);
        userRepository.update(user);

        UserLoginDto login = new UserLoginDto();
        login.setEmail(reg.getEmail());
        login.setPassword(reg.getPassword());

        assertThrows(UnauthorizedException.class, () -> userService.loginUser(login));
    }

    public void loginUser_InvalidPassword() throws Throwable {
        UserRegisterDto reg = createValidRegisterDto();
        userService.registerUser(reg);

        UserLoginDto login = new UserLoginDto();
        login.setEmail(reg.getEmail());
        login.setPassword("wrongpassword");

        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(login));
    }

    public void verifyEmail_Success() throws Throwable {
        UserRegisterDto reg = createValidRegisterDto();
        userService.registerUser(reg);
        User user = userRepository.findByEmail(reg.getEmail());

        EmailVerifyDto verifyDto = new EmailVerifyDto();
        verifyDto.setEmailVerifyToken(user.getEmailVerifyToken());

        BaseResponseDto<Object> res = userService.verifyEmail(user.getId(), verifyDto);
        assertEquals(EResponseCode.SUCCESS, res.getCode());

        User updated = userRepository.findById(user.getId());
        assertEquals(EUserVerifyStatus.Verified, updated.getVerifyStatus());
        assertNull(updated.getEmailVerifyToken());
    }

    public void verifyEmail_UserNotFound() throws Throwable {
        EmailVerifyDto dto = new EmailVerifyDto();
        dto.setEmailVerifyToken("token");
        assertThrows(ResourceNotFoundException.class, () -> userService.verifyEmail(999L, dto));
    }

    public void verifyEmail_BannedUser() throws Throwable {
        UserRegisterDto reg = createValidRegisterDto();
        userService.registerUser(reg);
        User user = userRepository.findByEmail(reg.getEmail());
        user.setVerifyStatus(EUserVerifyStatus.Banned);
        userRepository.update(user);

        EmailVerifyDto dto = new EmailVerifyDto();
        dto.setEmailVerifyToken("token");
        assertThrows(UnauthorizedException.class, () -> userService.verifyEmail(user.getId(), dto));
    }

    public void verifyEmail_AlreadyVerified() throws Throwable {
        UserRegisterDto reg = createValidRegisterDto();
        userService.registerUser(reg);
        User user = userRepository.findByEmail(reg.getEmail());
        user.setVerifyStatus(EUserVerifyStatus.Verified);
        userRepository.update(user);

        EmailVerifyDto dto = new EmailVerifyDto();
        dto.setEmailVerifyToken("any");
        BaseResponseDto<Object> res = userService.verifyEmail(user.getId(), dto);
        assertEquals("Email already verified", res.getMessage());
    }

    public void verifyEmail_InvalidToken() throws Throwable {
        UserRegisterDto reg = createValidRegisterDto();
        userService.registerUser(reg);
        User user = userRepository.findByEmail(reg.getEmail());

        EmailVerifyDto dto = new EmailVerifyDto();
        dto.setEmailVerifyToken("invalid-token-string");
        assertThrows(ValidationException.class, () -> userService.verifyEmail(user.getId(), dto));
    }

    public void verifyEmail_TokenUserIdMismatch() throws Throwable {
        UserRegisterDto reg1 = createValidRegisterDto();
        reg1.setEmail("u1@ex.com");
        userService.registerUser(reg1);
        User user1 = userRepository.findByEmail("u1@ex.com");

        UserRegisterDto reg2 = createValidRegisterDto();
        reg2.setEmail("u2@ex.com");
        userService.registerUser(reg2);
        User user2 = userRepository.findByEmail("u2@ex.com");

        EmailVerifyDto dto = new EmailVerifyDto();
        dto.setEmailVerifyToken(user1.getEmailVerifyToken());

        assertThrows(ValidationException.class, () -> userService.verifyEmail(user2.getId(), dto));
    }

    public void logoutUser_Success() throws Throwable {
        UserRegisterDto reg = createValidRegisterDto();
        var regRes = userService.registerUser(reg);
        User user = userRepository.findByEmail(reg.getEmail());

        UserLogoutDto logoutDto = new UserLogoutDto();
        logoutDto.setRefreshToken(regRes.getResult().getRefreshToken());

        String accessToken = regRes.getResult().getAccessToken();
        var claims = JwtUtils.decodeToken(accessToken, JwtUtils.getAccessSecret());

        BaseResponseDto<Object> res = userService.logoutUser(logoutDto, accessToken, claims);
        assertEquals(EResponseCode.SUCCESS, res.getCode());
        assertFalse(refreshTokenRepository.existsByToken(logoutDto.getRefreshToken()));
        assertTrue(TokenBlacklist.isBlacklisted(accessToken));
    }

    public void logoutUser_InvalidRefreshToken() throws Throwable {
        UserLogoutDto dto = new UserLogoutDto();
        dto.setRefreshToken("invalid");
        var claims = JwtUtils.decodeToken(JwtUtils.signAccessToken(1L, "Verified"), JwtUtils.getAccessSecret());
        assertThrows(UnauthorizedException.class, () -> userService.logoutUser(dto, "any", claims));
    }

    public void logoutUser_TokenMismatch() throws Throwable {
        UserRegisterDto reg1 = createValidRegisterDto();
        var res1 = userService.registerUser(reg1);

        UserRegisterDto reg2 = createValidRegisterDto();
        reg2.setEmail("u2@ex.com");
        var res2 = userService.registerUser(reg2);

        UserLogoutDto dto = new UserLogoutDto();
        dto.setRefreshToken(res1.getResult().getRefreshToken());

        String at2 = res2.getResult().getAccessToken();
        var claims2 = JwtUtils.decodeToken(at2, JwtUtils.getAccessSecret());

        assertThrows(UnauthorizedException.class, () -> userService.logoutUser(dto, at2, claims2));
    }

    public void logoutUser_TokenNotExist() throws Throwable {
        UserRegisterDto reg = createValidRegisterDto();
        var res = userService.registerUser(reg);

        UserLogoutDto dto = new UserLogoutDto();
        String validFormatTokenNotInDb = JwtUtils.signRefreshToken(999L, "Verified");
        dto.setRefreshToken(validFormatTokenNotInDb);

        var claims = JwtUtils.decodeToken(JwtUtils.signAccessToken(999L, "Verified"), JwtUtils.getAccessSecret());
        assertThrows(UnauthorizedException.class, () -> userService.logoutUser(dto, "any", claims));
    }

    public void refreshToken_Success() throws Throwable {
        UserRegisterDto reg = createValidRegisterDto();
        var res = userService.registerUser(reg);
        String oldToken = res.getResult().getRefreshToken();

        Thread.sleep(10);

        TokenRefreshDto refreshDto = new TokenRefreshDto();
        refreshDto.setRefreshToken(oldToken);

        BaseResponseDto<TokenRefreshResponseDto> refreshRes = userService.refreshToken(refreshDto);
        assertEquals(EResponseCode.SUCCESS, refreshRes.getCode());

        String newToken = refreshRes.getResult().getRefreshToken();
        assertNotEquals(oldToken, newToken);

        assertFalse(refreshTokenRepository.existsByToken(oldToken));
        assertTrue(refreshTokenRepository.existsByToken(newToken));
    }

    public void refreshToken_InvalidToken() throws Throwable {
        TokenRefreshDto dto = new TokenRefreshDto();
        dto.setRefreshToken("invalid");
        assertThrows(UnauthorizedException.class, () -> userService.refreshToken(dto));
    }

    public void refreshToken_TokenNotExist() throws Throwable {
        TokenRefreshDto dto = new TokenRefreshDto();
        dto.setRefreshToken(JwtUtils.signRefreshToken(1L, "Verified"));
        assertThrows(UnauthorizedException.class, () -> userService.refreshToken(dto));
    }

    public void refreshToken_UserNotFound() throws Throwable {
        User user = new User();
        user.setEmail("dummy@ex.com");
        user.setName("Dummy");
        user.setVerifyStatus(EUserVerifyStatus.Verified);
        userRepository.save(user);
        Long savedId = user.getId();

        String token = JwtUtils.signRefreshToken(savedId, "Verified");

        RefreshToken rt = new RefreshToken();
        rt.setUserId(savedId);
        rt.setToken(token);
        rt.setIat(java.time.OffsetDateTime.now());
        rt.setExp(java.time.OffsetDateTime.now().plusDays(1));
        refreshTokenRepository.save(rt);

        var conn = DatabaseManager.getConnection();
        try (var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM refresh_tokens WHERE user_id = " + savedId);
            stmt.execute("DELETE FROM users WHERE id = " + savedId);
        }

        TokenRefreshDto dto = new TokenRefreshDto();
        dto.setRefreshToken(JwtUtils.signRefreshToken(9999L, "Verified"));

    }

    private UserRegisterDto createValidRegisterDto() {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        dto.setName("Test User");
        dto.setDateOfBirth(LocalDate.of(2000, 1, 15));
        return dto;
    }
}
