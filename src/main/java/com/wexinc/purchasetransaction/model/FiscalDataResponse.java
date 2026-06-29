package com.wexinc.purchasetransaction.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class FiscalDataResponse {
    private List<ExchangeRateData> data;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ExchangeRateData {
        @JsonProperty("country_currency_desc")
        private String countryCurrencyDesc;

        @JsonProperty("exchange_rate")
        private BigDecimal exchangeRate;

        @JsonProperty("record_date")
        private LocalDate recordDate;
    }
}
