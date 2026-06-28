package com.wexinc.purchasetransaction.exception;

public class PurchaseNotFoundException extends RuntimeException {
    public PurchaseNotFoundException(String id) {
        super("Purchase with " + id + " not found");
    }
}
