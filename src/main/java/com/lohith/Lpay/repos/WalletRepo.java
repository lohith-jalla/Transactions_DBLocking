package com.lohith.Lpay.repos;

import com.lohith.Lpay.entities.Wallet;
import jakarta.persistence.LockModeType;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepo extends JpaRepository<Wallet, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.user.userId = :userId")
    Optional<Wallet> findByUserIdForUpdate(Long userId);

    Wallet findWalletByUser_UserId(Long userUserId);

    Object getWalletByWalletId(Long walletId);

    Long getWalletByUser_UserId(Long userUserId);
}
