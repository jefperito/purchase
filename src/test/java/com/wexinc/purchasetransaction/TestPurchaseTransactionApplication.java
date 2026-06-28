package com.wexinc.purchasetransaction;

import org.springframework.boot.SpringApplication;

public class TestPurchaseTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.from(PurchaseTransactionApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
