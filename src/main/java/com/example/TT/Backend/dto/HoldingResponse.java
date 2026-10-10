package com.example.TT.Backend.dto;

import java.math.BigDecimal;

public class HoldingResponse {
    private Long id;
    private String stockSymbol;
    private Integer quantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal unrealizedProfitLoss;

    public HoldingResponse() {
    }

    public HoldingResponse(Long id, String stockSymbol, Integer quantity, BigDecimal averagePrice,
                           BigDecimal currentPrice, BigDecimal currentValue, BigDecimal unrealizedProfitLoss) {
        this.id = id;
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.currentPrice = currentPrice;
        this.currentValue = currentValue;
        this.unrealizedProfitLoss = unrealizedProfitLoss;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public BigDecimal getUnrealizedProfitLoss() {
        return unrealizedProfitLoss;
    }

    public void setUnrealizedProfitLoss(BigDecimal unrealizedProfitLoss) {
        this.unrealizedProfitLoss = unrealizedProfitLoss;
    }
}
