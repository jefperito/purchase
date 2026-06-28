package com.wexinc.purchasetransaction.client;

import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

@Service
public class FiscalDataTreasuryApi {

    private WebClient client;

    @PostConstruct
    private void buildClient() {
        this.client = WebClient.builder()
            .baseUrl("https://api.fiscaldata.treasury.gov/services/api/fiscal_service")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    public FiscalDataResponse getData(final String currencyDescription) {
        var uri = String.format(
            "/v1/accounting/od/rates_of_exchange" +
                "?fields=country_currency_desc,exchange_rate,record_date" +
                "&filter=record_date:gte:%s,country_currency_desc:in:(%s)",
            LocalDate.now().minusMonths(6),
            currencyDescription
        );

        return client.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(FiscalDataResponse.class)
            .block();
    }
}
