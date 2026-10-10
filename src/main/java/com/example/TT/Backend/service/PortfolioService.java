package com.example.TT.Backend.service;

import com.example.TT.Backend.dto.HoldingResponse;
import com.example.TT.Backend.dto.PortfolioResponse;
import com.example.TT.Backend.model.Holding;
import com.example.TT.Backend.model.User;
import com.example.TT.Backend.repository.HoldingRepository;
import com.example.TT.Backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PortfolioService {

    private final HoldingRepository holdingRepository;
    private final UserRepository userRepository;

    public PortfolioService(HoldingRepository holdingRepository, UserRepository userRepository) {
        this.holdingRepository = holdingRepository;
        this.userRepository = userRepository;
    }

    public PortfolioResponse getPortfolio() {
        User user = getCurrentUser();
        List<Holding> holdings = holdingRepository.findByUser(user);

        List<HoldingResponse> holdingResponses = holdings.stream().map(this::toResponse).toList();

        BigDecimal portfolioValue = holdingResponses.stream()
            .map(HoldingResponse::getCurrentValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal unrealizedProfitLoss = holdingResponses.stream()
            .map(HoldingResponse::getUnrealizedProfitLoss)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        return new PortfolioResponse(user.getBalance(), portfolioValue, unrealizedProfitLoss, holdingResponses);
    }

    private HoldingResponse toResponse(Holding holding) {
        BigDecimal currentPrice = holding.getStock().getCurrentPrice();
        BigDecimal quantity = BigDecimal.valueOf(holding.getQuantity());
        BigDecimal currentValue = currentPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        BigDecimal costBasis = holding.getAveragePrice().multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        BigDecimal unrealizedProfitLoss = currentValue.subtract(costBasis).setScale(2, RoundingMode.HALF_UP);

        return new HoldingResponse(
            holding.getId(),
            holding.getStock().getSymbol(),
            holding.getQuantity(),
            holding.getAveragePrice(),
            currentPrice,
            currentValue,
            unrealizedProfitLoss
        );
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
    }
}
