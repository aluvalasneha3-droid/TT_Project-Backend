package com.example.TT.Backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class PortfolioResponse {
    private BigDecimal balance;
    private BigDecimal portfolioValue;
    private BigDecimal unrealizedProfitLoss;
    private List<HoldingResponse> holdings;

    public PortfolioResponse() {
    }

    public PortfolioResponse(BigDecimal balance, BigDecimal portfolioValue, BigDecimal unrealizedProfitLoss, List<HoldingResponse> holdings) {
        this.balance = balance;
        this.portfolioValue = portfolioValue;
        this.unrealizedProfitLoss = unrealizedProfitLoss;
        this.holdings = holdings;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getPortfolioValue() {
        return portfolioValue;
    }

    public void setPortfolioValue(BigDecimal portfolioValue) {
        this.portfolioValue = portfolioValue;
    }

    public BigDecimal getUnrealizedProfitLoss() {
        return unrealizedProfitLoss;
    }

    public void setUnrealizedProfitLoss(BigDecimal unrealizedProfitLoss) {
        this.unrealizedProfitLoss = unrealizedProfitLoss;
    }

    public List<HoldingResponse> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<HoldingResponse> holdings) {
        this.holdings = holdings;
    }
}
