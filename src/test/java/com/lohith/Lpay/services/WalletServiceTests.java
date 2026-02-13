package com.lohith.Lpay.services;

import com.lohith.Lpay.entities.Role;
import com.lohith.Lpay.entities.User;
import com.lohith.Lpay.entities.Wallet;
import com.lohith.Lpay.exceptions.InsufficientBalanceException;
import com.lohith.Lpay.exceptions.InvalidAmountException;
import com.lohith.Lpay.exceptions.WalletNotFoundException;
import com.lohith.Lpay.repos.TransactionRepo;
import com.lohith.Lpay.repos.WalletRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class WalletServiceTests {

    @Mock
    private WalletRepo walletRepo;

    @Mock
    private TransactionRepo transactionRepo;

    @InjectMocks
    private WalletService walletService;

    @Test
    @DisplayName("Should Debit money from sender and credit it to receiver")
    public void TransferMoneyTest_ShouldSuccessfullyDebitFromSenderCreditToReceiver(){

        // ********* Arrange ****************
        BigDecimal amount = new BigDecimal("20");

        User user1 = new User(5L,"Java","jav@123","password",(Integer) 123, Role.USER,LocalDateTime.now());
        User user2 = new User(6L,"Java12","jav12@123","password",(Integer) 123, Role.USER,LocalDateTime.now());

        Wallet sender = new Wallet();
        sender.setWalletId(3L);
        sender.setUser(user1);
        sender.setBalance(new BigDecimal("100"));

        Wallet receiver = new Wallet();
        receiver.setWalletId(4L);
        receiver.setUser(user2);
        receiver.setBalance(new BigDecimal("100"));

        when(walletRepo.findByUserIdForUpdate(5L)).thenReturn(Optional.of(sender));
        when(walletRepo.findByUserIdForUpdate(6L)).thenReturn(Optional.of(receiver));

        // *********** Act *************
        walletService.transferMoney(5L,6L,amount,"testTransfer-2");

        //********** Assert ***********
        assertEquals(new BigDecimal("120"), receiver.getBalance(),"Amount Credited Successfully");
        assertEquals(new BigDecimal("80"), sender.getBalance(),"Amount Debited Successfully");

        verify(walletRepo,times(2)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should credit money into user Account")
    public void TestCredit_ShouldSuccessfullyCreditTOUser(){
        // ************** Arrange ****************
        BigDecimal amount = new BigDecimal("20");

        User user1 = new User(1L,"Lohith","lohith12@gmail.com","password",(Integer) 123, Role.USER,LocalDateTime.now());

        Wallet sender = new Wallet();
        sender.setWalletId(1L);
        sender.setUser(user1);
        sender.setBalance(new BigDecimal("100"));

        when(walletRepo.findByUserIdForUpdate(1L)).thenReturn(Optional.of(sender)); // When there is a repo call, we use this
        // when(called db for some value).thenReturn(then return that value);

        // ***************** Act ***************
        walletService.creditMoney(1L,amount,"testCredit-2");

        // ************** Assert ***************
        assertEquals(new BigDecimal("120"),sender.getBalance(),"Amount Credited Successfully");

        verify(walletRepo,times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should debit money from user")
    public void testDebit_ShouldSuccessfullyDebitFromUser(){
        // ************** Arrange *************
        BigDecimal amount = new BigDecimal("20");

        User user1 = new User(1L,"Lohith","lohithjalla12@gmail.com","password",(Integer) 123, Role.USER,LocalDateTime.now());
        Wallet wallet= new Wallet();
        wallet.setWalletId(1L);
        wallet.setUser(user1);
        wallet.setBalance(new BigDecimal("100"));

        // *************** Act ******************

        when(walletRepo.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        walletService.debitMoney(1L,amount,"testDebit-2");

        // ************* Assert *****************
        assertEquals(new BigDecimal("80"),wallet.getBalance(),"Amount Debited Successfully");

        verify(walletRepo,times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Insufficient Balance Test")
    public void testDebit_InSufficientBalanceMustThrowException(){
        BigDecimal amount = new BigDecimal("20");

        User user = new User();
        user.setUserId(1L);
        user.setName("Lohith");

        Wallet wallet = new Wallet();
        wallet.setWalletId(1L);
        wallet.setUser(user);
        wallet.setBalance(new BigDecimal("0"));

        when(walletRepo.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientBalanceException.class,()->walletService.debitMoney(1L,amount,"testDebit-2"),"Insufficient Balance");
    }

    @Test
    @DisplayName("Invalid Amount Test")
    public void testCredit_InValidAmountMustThrowException(){
        BigDecimal amount = new BigDecimal("0");

        User user = new User();
        user.setUserId(1L);
        user.setName("Lohith");

        Wallet wallet = new Wallet();
        wallet.setWalletId(1L);
        wallet.setUser(user);
        wallet.setBalance(new BigDecimal("0"));

        assertThrows(InvalidAmountException.class,()->walletService.creditMoney(1L,amount,"testCredit-2"),"Insufficient Balance");
    }

    @Test
    @DisplayName("Sender Not Found Exception")
    public void testTransfer_SenderNotFoundMustThrowException(){
        BigDecimal amount = new BigDecimal("10");

        User user = new User();
        user.setUserId(1L);
        user.setName("Lohith");

        Wallet sender = new Wallet();
        sender.setWalletId(1L);
        sender.setBalance(new BigDecimal("100"));

        when(walletRepo.findByUserIdForUpdate(2L)).thenReturn(Optional.of(sender));

        assertThrows(WalletNotFoundException.class,() -> walletService.transferMoney(2L,1L,amount,"transferTest-6"));
    }

}
