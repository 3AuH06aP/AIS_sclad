package com.example.aisstock.model;

public enum StockTransactionType {
    RECEIPT,
    ISSUE,
    PUTAWAY,
    PICKING,
    PACKING,
    SHIPPING;

    public boolean isOutgoing() {
        return this == ISSUE || this == PICKING || this == SHIPPING;
    }
}
