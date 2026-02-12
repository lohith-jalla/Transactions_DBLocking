package com.lohith.Lpay.services;


import com.lohith.Lpay.dtos.*;
import com.lohith.Lpay.entities.RefreshToken;
import com.lohith.Lpay.entities.Role;
import com.lohith.Lpay.entities.User;
import com.lohith.Lpay.entities.Wallet;
import com.lohith.Lpay.repos.TokenRepo;
import com.lohith.Lpay.repos.UserRepo;
import com.lohith.Lpay.repos.WalletRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final WalletRepo walletRepo;
    private final TokenRepo tokenRepo;

    @Transactional
    public AuthResponse login(@RequestBody LoginRequest request) {
        User user = (User) userRepo.findByEmail((request.getEmail()));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect Credentials"+request.getPassword()+"---"+user.getPassword());
        }

        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken()
        );

    }

    @Transactional
    public void logout(String request) {

        RefreshToken refreshToken = refreshTokenService.getRefreshToken(request);
        if(refreshToken != null) {
            refreshToken.setRevoked(true);
            tokenRepo.save(refreshToken);
        }
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {

        RefreshToken refreshToken = refreshTokenService.getRefreshToken(request.getRefreshToken());
        if(refreshToken == null) {
            throw new RuntimeException("Invalid Refresh Token");
        }

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }

        if (refreshToken.getExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        // 🔁 Rotate refresh token (VERY IMPORTANT)
        refreshToken.setRevoked(true);

        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(refreshToken.getUser());

        String newAccessToken =
                jwtService.generateAccessToken(refreshToken.getUser());

        return new AuthResponse(
                newAccessToken,
                newRefreshToken.getToken()
        );
    }

    public RegisterResponse registerUser(RegisterRequest request) {
        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTransactionPin(request.getTransactionPin());
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());

        userRepo.save(user);

        Wallet wallet = new Wallet();

        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);

        walletRepo.save(wallet);

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new RegisterResponse(
                user.getName(),
                user.getEmail(),
                accessToken,
                refreshToken.getToken()
        );
    }

}
