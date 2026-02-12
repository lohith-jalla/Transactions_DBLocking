package com.lohith.Lpay.controllers;


import com.lohith.Lpay.dtos.*;
import com.lohith.Lpay.entities.User;
import com.lohith.Lpay.services.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/balance")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BalanceResponse> getBalance(
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        if(user!=null) {
            BalanceResponse balanceResponse = new BalanceResponse(walletService.getBalance(user.getUserId()));
            return ResponseEntity.ok(balanceResponse);
        }

        return new  ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/credit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CreditResponse> credit(
            @Valid @RequestBody CreditRequest request,
            Authentication authentication
    ){
        User user =  (User) authentication.getPrincipal();

        if(user!=null) {
            var result = walletService.creditMoney(user.getUserId(), request.getAmount(), request.getIdempotentKey());
            CreditResponse response = new CreditResponse(
                    result.getBalance(),
                    result.getTransactionId()
            );

            return new ResponseEntity<>(response,HttpStatus.CREATED);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/debit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DebitResponse> debit(
            @Valid @RequestBody DebitRequest request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        if(user!=null) {
            var result = walletService.debitMoney(
                    user.getUserId(),
                    request.getAmount(),
                    request.getIdempotencyKey()
            );

            DebitResponse response = new DebitResponse(
                    result.getBalance(),
                    result.getTransactionId()
            );

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {

        User fromUser = (User) authentication.getPrincipal();

        if(fromUser!=null) {
            var result = walletService.transferMoney(
                    fromUser.getUserId(),
                    request.getToUserId(),
                    request.getAmount(),
                    request.getIdempotencyKey()
            );

            TransferResponse response = new TransferResponse(
                    result.getBalance(),
                    result.getTransactionId()
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
