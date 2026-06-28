package com.wexinc.purchasetransaction;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wexinc.purchasetransaction.api.PurchaseDTO;
import com.wexinc.purchasetransaction.repository.PurchaseRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PurchaseRepository repository;

    private JsonMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();
    }

    @Test
    void should_save_transaction() throws Exception {
        var dto = buildTransactionDTO();

        var idempotencyKey = UUID.randomUUID().toString();
        mockMvc.perform(MockMvcRequestBuilders.post("/purchases")
            .contentType(MediaType.APPLICATION_JSON)
            .header("x-idempotency-key", idempotencyKey)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

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
        mockMvc.perform(MockMvcRequestBuilders.post("/purchases")
            .contentType(MediaType.APPLICATION_JSON)
            .header("x-idempotency-key", idempotencyKey)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.post("/purchases")
            .contentType(MediaType.APPLICATION_JSON)
            .header("x-idempotency-key", idempotencyKey)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(MockMvcResultMatchers.status().is(409))
            .andReturn();
    }

    private static PurchaseDTO buildTransactionDTO() {
        var transactionDate = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);
        return new PurchaseDTO("abcde", transactionDate, new BigDecimal("10.99"));
    }
}
