package com.wexinc.purchasetransaction.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity(name = "purchase")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(unique = true)
    private String idempotencyKey;

    private String description;

    private OffsetDateTime transactionDate;

    private BigDecimal amount;
}
