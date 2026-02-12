package com.lohith.Lpay.services;


import com.lohith.Lpay.dtos.RefreshRequest;
import com.lohith.Lpay.entities.RefreshToken;
import com.lohith.Lpay.entities.User;
import com.lohith.Lpay.repos.TokenRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RefreshTokenService {

    private TokenRepo tokenRepo;

    public RefreshToken createRefreshToken(User user) {

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiry(LocalDateTime.now().plusDays(7));
        token.setRevoked(false);

        return tokenRepo.save(token);
    }

    public RefreshToken getRefreshToken(String token) {
        return tokenRepo.findByToken(token);
    }
}
