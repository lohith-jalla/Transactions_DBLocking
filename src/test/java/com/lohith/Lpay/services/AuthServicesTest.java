package com.lohith.Lpay.services;


import com.lohith.Lpay.dtos.AuthResponse;
import com.lohith.Lpay.dtos.LoginRequest;
import com.lohith.Lpay.dtos.RegisterRequest;
import com.lohith.Lpay.dtos.RegisterResponse;
import com.lohith.Lpay.entities.RefreshToken;
import com.lohith.Lpay.entities.Role;
import com.lohith.Lpay.entities.User;
import com.lohith.Lpay.entities.Wallet;
import com.lohith.Lpay.repos.TokenRepo;
import com.lohith.Lpay.repos.UserRepo;
import com.lohith.Lpay.repos.WalletRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
public class AuthServicesTest {

    @Mock
    UserRepo userRepo;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthService authService;

    @Mock
    JwtService jwtService;

    @Mock
    WalletService walletService;

    @Mock
    WalletRepo walletRepo;

    @Mock
    RefreshTokenService refreshTokenService;

    @Mock
    TokenRepo tokenRepo;

    @Test
    public void testLogin(){
        // ******************* Arrange **************
        String email = "email@gmail.com";
        String password = "password";

        User user = new User();
        user.setUserId(1L);
        user.setPassword(password);
        user.setEmail(email);
        user.setName("name");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());


        when(userRepo.findByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches(password,user.getPassword())).thenReturn(true);


        String accessToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyYWRoaWthQGdtYWlsLmNvbSIsInVzZXJJZCI6IjQiLCJyb2xlIjoiVVNFUiIsImlhdCI6MTc3MDc4NjQ4OSwiZXhwIjoxNzcwNzkwMDg5fQ.zwzlykINh9i1jZ5F4Dz-DhOAX4eZlYPh2c9GGW_FM_E";
        RefreshToken refreshToken  = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken("25226b05-4d6a-4ca2-85f5-8007f099c203");
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setRevoked(false);
        refreshToken.setExpiry(LocalDateTime.now().plusDays(10));


        when(jwtService.generateAccessToken(user)).thenReturn(accessToken);
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);


        // ************** Act ******************
        LoginRequest loginRequest = new LoginRequest(email,password);
        AuthResponse response = authService.login(loginRequest);

        // ************* Assert ***************
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken.getToken(), response.getRefreshToken());

        verify(jwtService, times(1)).generateAccessToken(user);
        verify(refreshTokenService,times(1)).createRefreshToken(user);
    }

    @Test
    public void testRegister() {
        // *********** 1. Arrange **************
        // We only need to define what the Mocks should return.
        // We don't need to manually create the 'user' for verify anymore.

        String accessToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyYWRoaWthQGdtYWlsLmNvbS...";
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("25226b05-4d6a-4ca2-85f5-8007f099c203");

        // Stubbing: Accept ANY user object since the service creates its own instance
        when(jwtService.generateAccessToken(any(User.class))).thenReturn(accessToken);
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);

        // ************ 2. Act *****************
        RegisterRequest registerRequest = new RegisterRequest("Lohith", "lohith12@gmail.com", "password", 1234);
        RegisterResponse registerResponse = authService.registerUser(registerRequest);

        // *********** 3. Assert (Response) ***************
        assertNotNull(registerResponse);
        assertEquals(registerRequest.getEmail(), registerResponse.getEmail());
        assertEquals(registerRequest.getName(), registerResponse.getName());
        assertEquals(accessToken, registerResponse.getAccessToken());
        assertEquals(refreshToken.getToken(), registerResponse.getRefreshToken());

        // *********** 4. Verify & Capture (The Fix) ***************

        // Capture the User object that was created inside AuthService
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        // Verify the data inside the captured user matches the request
        assertEquals("Lohith", savedUser.getName());
        assertEquals("lohith12@gmail.com", savedUser.getEmail());

        // For the wallet, since it's also created inside, use any() or another captor
        verify(walletRepo, times(1)).save(any(Wallet.class));
    }

    @Test
    public void logoutTest(){
        // ************** Arrange *****************

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("25226b05-4d6a-4ca2-85f5-8007f099c203");

        when(refreshTokenService.getRefreshToken(any(String.class))).thenReturn(refreshToken);

        // ************* ACT ***********************
        authService.logout(refreshToken.getToken());

        // ************* Assert *******************

        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(tokenRepo,times(1)).save(refreshTokenCaptor.capture());

        RefreshToken token = refreshTokenCaptor.getValue();
        assertEquals(token.getToken(), refreshToken.getToken());
        assertTrue(token.isRevoked());
    }


}
