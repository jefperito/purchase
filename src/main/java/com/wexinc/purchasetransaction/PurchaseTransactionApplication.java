package com.wexinc.purchasetransaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(
    scanBasePackages = { "com.wexinc.purchasetransaction"}
)
@EnableCaching
public class PurchaseTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PurchaseTransactionApplication.class, args);
    }
}
