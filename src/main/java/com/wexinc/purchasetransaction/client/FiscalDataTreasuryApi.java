package com.wexinc.purchasetransaction.client;

import com.wexinc.purchasetransaction.entity.Purchase;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class FiscalDataTreasuryApi {

    private WebClient client;

    private final String url;

    public FiscalDataTreasuryApi(
            @Value("${fiscal.api.base-url}") String url) {
        this.url = url;
    }

    @PostConstruct
    private void buildClient() {
        this.client = WebClient.builder()
            .baseUrl(url)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    // TODO Cache into Redis TTL 24h
    public FiscalDataResponse getData(final Purchase purchase, final String currencyDescription) {
        var uri = String.format(
            "/v1/accounting/od/rates_of_exchange" +
                "?fields=country_currency_desc,exchange_rate,record_date" +
                "&filter=record_date:gte:%s,country_currency_desc:in:(%s)",
            purchase.getTransactionDate().toLocalDate().minusMonths(6),
            currencyDescription
        );

        return client.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(FiscalDataResponse.class)
            .retryWhen(Retry.backoff(3, Duration.ofMillis(500)))
            .block();
    }
}
