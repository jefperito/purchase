package com.wexinc.purchasetransaction.api;

import com.wexinc.purchasetransaction.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("purchases")
@RequiredArgsConstructor
@Slf4j
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping("/{purchaseId}")
    public PurchaseDTO getPurchase(@PathVariable final String purchaseId,
                                   @RequestParam(required = false) final String currency) {
        return PurchaseDTO.fromEntity(purchaseService.retrieve(purchaseId, currency));
    }

    @PostMapping
    public PurchaseDTO persist(
            @RequestHeader("x-idempotency-key") final String idempotencyKey,
            @Valid @RequestBody final PurchaseDTO purchaseDTO) {
        return PurchaseDTO.fromEntity(purchaseService.save(idempotencyKey, purchaseDTO));
    }
}
