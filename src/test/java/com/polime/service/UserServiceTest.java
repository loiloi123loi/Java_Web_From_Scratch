package com.polime.service;

import java.time.LocalDate;

import com.polime.core.DatabaseManager;
import com.polime.dto.BaseResponseDto;
import com.polime.dto.user.request.UserRegisterDto;
import com.polime.dto.user.response.UserRegisterResponseDto;
import com.polime.enums.EResponseCode;
import com.polime.exception.DuplicateResourceException;
import com.polime.model.User;
import com.polime.repository.RefreshTokenRepository;
import com.polime.repository.UserRepository;
import com.polime.test.BaseServiceTest;

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
