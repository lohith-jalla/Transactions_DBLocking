package com.lohith.Lpay.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletOperationResult {

    private BigDecimal balance;
    private Long transactionId;
}
