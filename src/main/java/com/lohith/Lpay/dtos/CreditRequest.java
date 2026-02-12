package com.lohith.Lpay.dtos;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditRequest {

    @NotNull
    @DecimalMin(value = "0.01" , message = "Amount should be grater than Zero")
    private BigDecimal amount;

    @NotBlank
    private String idempotentKey;
}
