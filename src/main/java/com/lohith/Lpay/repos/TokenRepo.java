package com.lohith.Lpay.repos;

import com.lohith.Lpay.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepo extends JpaRepository<RefreshToken,Long> {
    RefreshToken findByToken(String token);
}
