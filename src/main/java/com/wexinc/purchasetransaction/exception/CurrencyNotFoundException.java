package com.wexinc.purchasetransaction.exception;

public class CurrencyNotFoundException extends RuntimeException {
    public CurrencyNotFoundException(String currency) {
        super("Currency " + currency + " not found");
    }
}
