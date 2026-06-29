package com.wexinc.purchasetransaction.model;

import java.math.BigDecimal;

public record CurrencyConverterResult(BigDecimal exchangeRate, BigDecimal convertedAmount) {
}
