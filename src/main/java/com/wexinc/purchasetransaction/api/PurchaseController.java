package com.wexinc.purchasetransaction.api;

import com.wexinc.purchasetransaction.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("purchases")
@RequiredArgsConstructor
@Slf4j
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping("/{purchaseId}")
    public PurchaseResponse getPurchase(@PathVariable final String purchaseId,
                                             @RequestParam(required = false) final String currency) {
        var purchase = purchaseService.retrieve(purchaseId);
        return Optional.ofNullable(currency)
            .map(c ->
                PurchaseResponse.fromEntity(purchase, purchaseService.calculateCurrency(purchase, currency)))
            .orElse(PurchaseResponse.fromEntity(purchase));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse persist(
            @RequestHeader("x-idempotency-key") final String idempotencyKey,
            @Valid @RequestBody final CreatePurchaseRequest purchaseDTO) {
        return PurchaseResponse.fromEntity(purchaseService.save(idempotencyKey, purchaseDTO));
    }
}
