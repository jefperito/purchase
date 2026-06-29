package com.wexinc.purchasetransaction.api;

import com.wexinc.purchasetransaction.model.Purchase;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PurchaseResponse(
        String id,
        String description,
        OffsetDateTime transactionDate,
        BigDecimal amount,
        BigDecimal convertedAmount) {

    public static PurchaseResponse fromEntity(final Purchase purchase, final BigDecimal convertedAmount) {
        return new PurchaseResponse(
            purchase.getId().toString(),
            purchase.getDescription(),
            purchase.getTransactionDate(),
            purchase.getAmount(), convertedAmount);
    }

    public static PurchaseResponse fromEntity(final Purchase purchase) {
        return new PurchaseResponse(
                purchase.getId().toString(),
                purchase.getDescription(),
                purchase.getTransactionDate(),
                purchase.getAmount(),
                purchase.getAmount());
    }
}
