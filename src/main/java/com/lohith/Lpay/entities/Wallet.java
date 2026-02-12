package com.lohith.Lpay.entities;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // For real life money transactions, Real-World applications don't use Double or Float,
    // they Use BigDecimal or Long (Stores as no.of paise/cents/yen etc.)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
    // when updating in db, we also make use of the version so that
    // when multiple users need to write to db, the version helps in track of
    // which version they need to update to.
    // Ex:- if v=1 and i performed a write operation, then v becomes 2;
    // so if a user wants to write to version 1, they can't.

    @Version
    private Long version;
}
