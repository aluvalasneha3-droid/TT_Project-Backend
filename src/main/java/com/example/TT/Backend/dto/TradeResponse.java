package com.example.TT.Backend.dto;

import java.math.BigDecimal;

public class TradeResponse {
    private String message;
    private String symbol;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal balance;

    public TradeResponse() {
    }

    public TradeResponse(String message, String symbol, Integer quantity, BigDecimal price, BigDecimal balance) {
        this.message = message;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.balance = balance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
