package com.wexinc.purchasetransaction.model;

import com.wexinc.purchasetransaction.entity.Purchase;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PurchaseResponse(
        String id,
        String description,
        OffsetDateTime transactionDate,
        BigDecimal amount,
        BigDecimal convertedAmount,
        BigDecimal exchangeRate) {

    public static PurchaseResponse fromEntity(final Purchase purchase, final CurrencyConverterResult
            result) {
        return new PurchaseResponse(
            purchase.getId().toString(),
            purchase.getDescription(),
            purchase.getTransactionDate(),
            purchase.getAmount(), result.convertedAmount(), result.exchangeRate());
    }

    public static PurchaseResponse fromEntity(final Purchase purchase) {
        return new PurchaseResponse(
            purchase.getId().toString(),
            purchase.getDescription(),
            purchase.getTransactionDate(),
            purchase.getAmount(),
            purchase.getAmount(),
            new BigDecimal("1.00"));
    }
}
