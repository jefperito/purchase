package com.wexinc.purchasetransaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wexinc.purchasetransaction.api.CreatePurchaseRequest;
import com.wexinc.purchasetransaction.api.PurchaseResponse;
import com.wexinc.purchasetransaction.client.FiscalDataResponse;
import com.wexinc.purchasetransaction.repository.PurchaseRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PurchaseRepository repository;

    private static MockWebServer mockBackEnd;

    private static JsonMapper objectMapper;

    @BeforeAll
    static void beforeAll() throws IOException {
        objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add(
            "fiscal.api.base-url",
            () -> mockBackEnd.url("/").toString());
    }

    @Test
    void should_save_transaction() throws Exception {
        var dto = buildTransactionDTO();

        var idempotencyKey = UUID.randomUUID().toString();
        mockMvc.perform(MockMvcRequestBuilders.post("/purchases")
            .contentType(MediaType.APPLICATION_JSON)
            .header("x-idempotency-key", idempotencyKey)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(MockMvcResultMatchers.status().isCreated());

        var purchases = repository.findAll();
        Assertions.assertFalse(purchases.isEmpty());
        Assertions.assertEquals(1, purchases.size());
        var savedPurchase = purchases.getFirst();
        Assertions.assertNotNull(savedPurchase.getId());
        Assertions.assertEquals(dto.description(), savedPurchase.getDescription());
        Assertions.assertEquals(dto.amount(), savedPurchase.getAmount());
        Assertions.assertEquals(dto.transactionDate().toInstant(), savedPurchase.getTransactionDate().toInstant());
        Assertions.assertEquals(idempotencyKey, savedPurchase.getIdempotencyKey());
    }

    @Test
    void should_avoid_duplicate_transaction() throws Exception {
        var dto = buildTransactionDTO();

        var idempotencyKey = UUID.randomUUID().toString();
        var firstResult = mockMvc.perform(MockMvcRequestBuilders.post("/purchases")
            .contentType(MediaType.APPLICATION_JSON)
            .header("x-idempotency-key", idempotencyKey)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andReturn();

        var secondResult = mockMvc.perform(MockMvcRequestBuilders.post("/purchases")
            .contentType(MediaType.APPLICATION_JSON)
            .header("x-idempotency-key", idempotencyKey)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andReturn();

        var first =
            objectMapper.readValue(
                firstResult.getResponse().getContentAsString(),
                PurchaseResponse.class);

        var second =
            objectMapper.readValue(
                secondResult.getResponse().getContentAsString(),
                PurchaseResponse.class);

        Assertions.assertEquals(first.id(), second.id());
    }

    @Test
    void should_get_transaction_with_dollar_currency() throws Exception {
        var savedPurchase = saveAPurchase();

        var getResponse = mockMvc.perform(MockMvcRequestBuilders.get("/purchases/" + savedPurchase.id())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()
            .getResponse();

        var getPurchase = objectMapper.readValue(getResponse.getContentAsString(), PurchaseResponse.class);

        Assertions.assertEquals(new BigDecimal("10.99"), getPurchase.amount());
        Assertions.assertEquals(new BigDecimal("10.99"), getPurchase.convertedAmount());
    }

    @Test
    void should_return_bad_request_when_try_with_invalid_currency() throws Exception {
        var savedPurchase = saveAPurchase();

        mockFiscalDataAPiWithoutData();

        var currency = "Invalid-real";
        mockMvc.perform(MockMvcRequestBuilders.get(
                "/purchases/" + savedPurchase.id() + "?currency=" + currency)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void should_get_transaction_with_most_recent_canadian_dollar_currency() throws Exception {
        var savedPurchase = saveAPurchase();

        mockFiscalDataAPi();

        var canadaDollarCurrency = "Canada-Dollar";
        var getResponse = mockMvc.perform(MockMvcRequestBuilders.get(
                "/purchases/" + savedPurchase.id() + "?currency=" + canadaDollarCurrency)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()
            .getResponse();

        var getPurchase = objectMapper.readValue(getResponse.getContentAsString(), PurchaseResponse.class);

        Assertions.assertEquals(new BigDecimal("10.99"), getPurchase.amount());
        Assertions.assertEquals(new BigDecimal("15.06"), getPurchase.convertedAmount());
    }

    private void mockFiscalDataAPiWithoutData() {
        mockBackEnd.enqueue(new MockResponse()
            .setBody("{\"data\":[]}")
            .addHeader("Content-Type", "application/json"));
    }

    private static void mockFiscalDataAPi() throws JsonProcessingException {
        var fiscalDataResponse = new FiscalDataResponse();
        fiscalDataResponse.setData(List.of(
            new FiscalDataResponse.ExchangeRateData(
                "Canada-Dollar",
                new BigDecimal("1.355"),
                LocalDate.of(2026, 3, 21)),
            new FiscalDataResponse.ExchangeRateData(
                "Canada-Dollar",
                new BigDecimal("1.37"),
                LocalDate.of(2026, 6, 21))
        ));

        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(fiscalDataResponse))
            .addHeader("Content-Type", "application/json"));
    }

    private PurchaseResponse saveAPurchase() throws Exception {
        var request = buildTransactionDTO();

        var idempotencyKey = UUID.randomUUID().toString();
        var saveResponse = mockMvc.perform(MockMvcRequestBuilders.post("/purchases")
            .contentType(MediaType.APPLICATION_JSON)
            .header("x-idempotency-key", idempotencyKey)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andReturn()
            .getResponse();

        return objectMapper.readValue(saveResponse.getContentAsString(), PurchaseResponse.class);
    }

    private static CreatePurchaseRequest buildTransactionDTO() {
        var transactionDate = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);
        return new CreatePurchaseRequest("abcde", transactionDate, new BigDecimal("10.99"));
    }
}
