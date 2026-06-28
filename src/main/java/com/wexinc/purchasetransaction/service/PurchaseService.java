package com.wexinc.purchasetransaction.service;

import com.wexinc.purchasetransaction.api.PurchaseDTO;
import com.wexinc.purchasetransaction.client.FiscalDataTreasuryApi;
import com.wexinc.purchasetransaction.exception.CurrencyNotFoundException;
import com.wexinc.purchasetransaction.exception.PurchaseNotFoundException;
import com.wexinc.purchasetransaction.model.Purchase;
import com.wexinc.purchasetransaction.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseService {

    private final PurchaseRepository repository;

    private final FiscalDataTreasuryApi dataTreasuryApi;

    public Purchase retrieve(final String purchaseId, final String currency) {
        var purchase = repository.findById(UUID.fromString(purchaseId))
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));
        Optional.ofNullable(currency).ifPresent(c -> {
            var response = dataTreasuryApi.getData(currency);
            if (response.getData().isEmpty()) {
                throw new CurrencyNotFoundException(currency);
            }
            response.getData().forEach(e -> log.info(e.toString()));
            // TODO convert the amount and round
        });
        return purchase;
    }

    public Purchase save(final String idempotencyKey, final PurchaseDTO purchaseDTO) {
        return repository.save(purchaseDTO.fromEntity(idempotencyKey));
    }
}
