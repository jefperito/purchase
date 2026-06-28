package com.wexinc.purchasetransaction.api;

import com.wexinc.purchasetransaction.exception.PurchaseNotFoundException;
import com.wexinc.purchasetransaction.model.Purchase;
import com.wexinc.purchasetransaction.repository.PurchaseRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseRepository repository;

    @GetMapping("/{purchaseId}")
    public PurchaseDTO getPurchase(@PathVariable final String purchaseId) {
        return PurchaseDTO.fromEntity(
            repository.findById(UUID.fromString(purchaseId))
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId)));
    }

    @PostMapping
    public PurchaseDTO persist(
            @RequestHeader("x-idempotency-key") final String idempotencyKey,
            @Valid @RequestBody final PurchaseDTO purchaseDTO) {
        Purchase savedPurchase = repository.save(purchaseDTO.fromEntity(idempotencyKey));
        return PurchaseDTO.fromEntity(savedPurchase);
    }
}
