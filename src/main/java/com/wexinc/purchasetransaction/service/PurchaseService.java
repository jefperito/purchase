package com.wexinc.purchasetransaction.service;

import com.wexinc.purchasetransaction.api.CreatePurchaseRequest;
import com.wexinc.purchasetransaction.client.FiscalDataResponse;
import com.wexinc.purchasetransaction.client.FiscalDataTreasuryApi;
import com.wexinc.purchasetransaction.exception.CurrencyNotFoundException;
import com.wexinc.purchasetransaction.exception.PurchaseNotFoundException;
import com.wexinc.purchasetransaction.model.Purchase;
import com.wexinc.purchasetransaction.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseService {

    private final PurchaseRepository repository;

    private final FiscalDataTreasuryApi dataTreasuryApi;

    public Purchase retrieve(final String purchaseId) {
        return repository.findById(UUID.fromString(purchaseId))
            .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));
    }

    public BigDecimal calculateCurrency(final Purchase purchase, final String currency) {
        var response = dataTreasuryApi.getData(purchase, currency);
        if (response.getData().isEmpty()) {
            throw new CurrencyNotFoundException(currency);
        }
        var mostRecent =
            response.getData().stream()
                .max(Comparator.comparing(FiscalDataResponse.ExchangeRateData::getRecordDate))
                .orElseThrow(() -> new IllegalStateException("Empty result"));

        return purchase.getAmount().multiply(mostRecent.getExchangeRate()).setScale(2, RoundingMode.HALF_UP);
    }

    public Purchase save(final String idempotencyKey, final CreatePurchaseRequest purchaseDTO) {
        try {
            return repository.save(purchaseDTO.fromEntity(idempotencyKey));
        } catch (DataIntegrityViolationException e) {
            return repository.findByIdempotencyKey(idempotencyKey);
        }
    }
}
