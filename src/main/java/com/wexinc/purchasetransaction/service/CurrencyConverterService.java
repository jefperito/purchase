package com.wexinc.purchasetransaction.service;

import com.wexinc.purchasetransaction.client.FiscalDataResponse;
import com.wexinc.purchasetransaction.client.FiscalDataTreasuryApi;
import com.wexinc.purchasetransaction.entity.Purchase;
import com.wexinc.purchasetransaction.exception.CurrencyNotFoundException;
import com.wexinc.purchasetransaction.model.CurrencyConverterResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class CurrencyConverterService {

    private final FiscalDataTreasuryApi dataTreasuryApi;

    public CurrencyConverterResult calculateCurrency(final Purchase purchase, final String currency) {
        var response = dataTreasuryApi.getData(purchase, currency);
        if (response.getData().isEmpty()) {
            throw new CurrencyNotFoundException(currency);
        }
        var mostRecent =
            response.getData().stream()
                .max(Comparator.comparing(FiscalDataResponse.ExchangeRateData::getRecordDate))
                .orElseThrow(() -> new IllegalStateException("Empty result"));

        var convertedAmount = purchase.getAmount()
            .multiply(mostRecent.getExchangeRate())
            .setScale(2, RoundingMode.HALF_UP);

        return new CurrencyConverterResult(mostRecent.getExchangeRate(), convertedAmount);
    }
}
