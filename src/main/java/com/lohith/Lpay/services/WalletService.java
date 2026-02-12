package com.lohith.Lpay.services;

import com.lohith.Lpay.dtos.WalletOperationResult;
import com.lohith.Lpay.entities.Transaction;
import com.lohith.Lpay.entities.TransactionType;
import com.lohith.Lpay.entities.Wallet;
import com.lohith.Lpay.exceptions.DuplicateTransactionException;
import com.lohith.Lpay.exceptions.InsufficientBalanceException;
import com.lohith.Lpay.exceptions.InvalidAmountException;
import com.lohith.Lpay.exceptions.WalletNotFoundException;
import com.lohith.Lpay.repos.TransactionRepo;
import com.lohith.Lpay.repos.UserRepo;
import com.lohith.Lpay.repos.WalletRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepo userRepo;
    private final WalletRepo walletRepo;
    private final TransactionRepo transactionRepo;


    @Transactional // This @Transactional ensures that all the db operations
    // will succeed together (commit) or fail together (rollback)
    public WalletOperationResult transferMoney(
            Long fromUserId,
            Long toUserId,
            BigDecimal amount,
            String idempotentKey
    ){
        Optional<Transaction> existing = transactionRepo.findByIdempotencyKey(idempotentKey);
        if(existing.isPresent()){
            Wallet existWallet = walletRepo
                    .findByUserIdForUpdate(toUserId)
                    .orElseThrow(() -> new WalletNotFoundException("Receiver Wallet Not Found"+toUserId));
           return new WalletOperationResult(
                   existWallet.getBalance(),
                   existing.get().getTransactionId()
           );
        } // If the idempotent key, i.e., the key which is unique for every transfer already exists,
        // then don't process the request as it was already processed.

        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException("Amount must be greater than zero");
        } // If the Balance is <= 0, the amount must be >= 0

        Wallet fromWallet = walletRepo
                .findByUserIdForUpdate(fromUserId)
                .orElseThrow(() -> new WalletNotFoundException("Sender Wallet Not Found "+fromUserId)); // fetch sender wallet


        Wallet toWallet = walletRepo
                .findByUserIdForUpdate(toUserId)
                .orElseThrow(() -> new WalletNotFoundException("Receiver Wallet Not Found "+toUserId)); // fetch receiver wallet

        if(fromWallet.getBalance().compareTo(amount) < 0){
            throw new InsufficientBalanceException("Amount must be greater than zero");
        } // return -1, if balance < amount and 0, if amount == 0 and 1,if balance > amount.
        // so we must block the condition balance < amount, so we wrote < 0

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount)); // subtract amount from balance
        toWallet.setBalance(toWallet.getBalance().add(amount)); // add amount to balance

        walletRepo.save(fromWallet); // save wallet
        walletRepo.save(toWallet); // save wallet

        Transaction trx = new Transaction(); // create transaction Audit.
        trx.setFromWallet(fromWallet);
        trx.setToWallet(toWallet);
        trx.setAmount(amount);
        trx.setType(TransactionType.TRANSFER);
        trx.setCreatedAt(LocalDateTime.now());
        trx.setIdempotencyKey(idempotentKey);

        try {
            transactionRepo.save(trx);
        }catch(DataIntegrityViolationException ex){
            throw new DuplicateTransactionException("Duplicate transaction request");
        }

        return new WalletOperationResult(
                fromWallet.getBalance(),
                trx.getTransactionId()
        );

    }

    @Transactional
    public WalletOperationResult creditMoney(
            Long toUserId,
            BigDecimal amount,
            String idempotencyKey
    ) {
        Optional<Transaction> existing =
                transactionRepo.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            Wallet wallet = walletRepo
                    .findByUserIdForUpdate(toUserId)
                    .orElseThrow(() -> new WalletNotFoundException("Wallet Not Found"));

            return new WalletOperationResult(
                    wallet.getBalance(),
                    existing.get().getTransactionId()
            );
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }

        Wallet wallet = walletRepo
                .findByUserIdForUpdate(toUserId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet Not Found"));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepo.save(wallet);

        Transaction trx = new Transaction();
        trx.setFromWallet(null);
        trx.setToWallet(wallet);
        trx.setAmount(amount);
        trx.setType(TransactionType.CREDIT);
        trx.setCreatedAt(LocalDateTime.now());
        trx.setIdempotencyKey(idempotencyKey);

        try {
            transactionRepo.save(trx);
        }catch(DataIntegrityViolationException ex){
            throw new DuplicateTransactionException("Duplicate transaction request");
        }

        return new WalletOperationResult(
                wallet.getBalance(),
                trx.getTransactionId()
        );
    }


    @Transactional
    public WalletOperationResult debitMoney(
            Long fromUserId,
            BigDecimal amount,
            String idempotentKey
    ){
        Optional<Transaction> existing = transactionRepo.findByIdempotencyKey(idempotentKey);

        if(existing.isPresent()){
            Wallet existWallet = walletRepo
                    .findByUserIdForUpdate(fromUserId)
                    .orElseThrow(() -> new WalletNotFoundException("Wallet Not Found"));

            return new WalletOperationResult(
                    existWallet.getBalance(),
                    existing.get().getTransactionId()
            );
        }

        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidAmountException("Amount must be greater than zero");
        }


        Wallet userWallet = walletRepo
                .findByUserIdForUpdate(fromUserId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet Not Found"));

        if(userWallet.getBalance().compareTo(amount) < 0){
            throw new InsufficientBalanceException("Insufficient balance");
        }

        userWallet.setBalance(userWallet.getBalance().subtract(amount));

        walletRepo.save(userWallet);

        Transaction trx = new Transaction();
        trx.setFromWallet(userWallet);
        trx.setToWallet(null);
        trx.setAmount(amount);
        trx.setType(TransactionType.DEBIT);
        trx.setCreatedAt(LocalDateTime.now());
        trx.setIdempotencyKey(idempotentKey);

        try {
            transactionRepo.save(trx);
        }catch(DataIntegrityViolationException ex){
            throw new DuplicateTransactionException("Duplicate transaction request");
        }

        return new WalletOperationResult(
                userWallet.getBalance(),
                trx.getTransactionId()
        );
    }

    public BigDecimal getBalance(Long userId) {
        Wallet wallet = walletRepo.findWalletByUser_UserId(userId);
        if (wallet == null) {
            throw new WalletNotFoundException("Wallet Not Found");
        }
        return wallet.getBalance();

    }



}
