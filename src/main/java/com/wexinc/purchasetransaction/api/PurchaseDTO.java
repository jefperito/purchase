package com.wexinc.purchasetransaction.api;

import com.wexinc.purchasetransaction.model.Purchase;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PurchaseDTO(
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
        purchase.setAmount(amount);

        return purchase;
    }
    public static PurchaseDTO fromEntity(final Purchase purchase) {
        return new PurchaseDTO(
            purchase.getDescription(),
            purchase.getTransactionDate(),
            purchase.getAmount());
    }
}
