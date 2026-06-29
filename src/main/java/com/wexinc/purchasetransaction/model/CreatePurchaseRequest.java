package com.wexinc.purchasetransaction.model;

import com.wexinc.purchasetransaction.entity.Purchase;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

public record CreatePurchaseRequest(
        @NotBlank(message = "A descrição é obrigatória.")
        @Size(max = 50, message = "A descrição não pode passar de 50 caracteres.")
        String description,

        @NotNull(message = "transaction date é obrigatório.")
        OffsetDateTime transactionDate,

        @NotNull(message = "O valor é obrigatório.")
        @Positive(message = "O valor deve ser positivo e maior que zero.")
        @Digits(integer = 10, fraction = 2, message = "O valor deve ter no máximo 2 casas decimais (centavos).")
        BigDecimal amount) {
    public Purchase fromEntity(final String idempotencyKey) {
        var purchase = new Purchase();
        purchase.setIdempotencyKey(idempotencyKey);
        purchase.setDescription(description);
        purchase.setTransactionDate(transactionDate);
        purchase.setAmount(amount.setScale(2, RoundingMode.HALF_UP));

        return purchase;
    }
}
