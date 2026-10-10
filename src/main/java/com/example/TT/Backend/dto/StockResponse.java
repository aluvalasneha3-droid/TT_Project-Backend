package com.example.TT.Backend.dto;

import java.math.BigDecimal;

public class StockResponse {
    private Long id;
    private String symbol;
    private String companyName;
    private BigDecimal currentPrice;
    private BigDecimal netChange;
    private BigDecimal percentChange;
    private String updatedAt;
    private String dataSource = "DEMO";

    public StockResponse() {
    }

    public StockResponse(Long id, String symbol, String companyName, BigDecimal currentPrice) {
        this.id = id;
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getNetChange() {
        return netChange;
    }

    public void setNetChange(BigDecimal netChange) {
        this.netChange = netChange;
    }

    public BigDecimal getPercentChange() {
        return percentChange;
    }

    public void setPercentChange(BigDecimal percentChange) {
        this.percentChange = percentChange;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}
