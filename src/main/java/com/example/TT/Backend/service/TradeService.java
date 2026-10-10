package com.example.TT.Backend.service;

import com.example.TT.Backend.dto.TradeRequest;
import com.example.TT.Backend.dto.TradeResponse;
import com.example.TT.Backend.model.Holding;
import com.example.TT.Backend.model.Stock;
import com.example.TT.Backend.model.Transaction;
import com.example.TT.Backend.model.User;
import com.example.TT.Backend.repository.HoldingRepository;
import com.example.TT.Backend.repository.StockRepository;
import com.example.TT.Backend.repository.TransactionRepository;
import com.example.TT.Backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class TradeService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;
    private final StockService stockService;

    public TradeService(UserRepository userRepository,
                        StockRepository stockRepository,
                        HoldingRepository holdingRepository,
                        TransactionRepository transactionRepository,
                        StockService stockService) {
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
        this.holdingRepository = holdingRepository;
        this.transactionRepository = transactionRepository;
        this.stockService = stockService;
    }

    @Transactional
    public TradeResponse buy(TradeRequest request) {
        User user = getCurrentUser();
        Stock stock = getStock(request.getSymbol());
        int quantity = validateQuantity(request.getQuantity());
        BigDecimal price = stockService.getPriceForTrade(stock.getSymbol());

        BigDecimal cost = price.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        if (user.getBalance().compareTo(cost) < 0) {
            throw new IllegalArgumentException("Insufficient virtual balance for this purchase.");
        }

        user.setBalance(user.getBalance().subtract(cost).setScale(2, RoundingMode.HALF_UP));
        userRepository.save(user);

        Holding holding = holdingRepository.findByUserAndStock(user, stock).orElse(null);
        if (holding == null) {
            holding = new Holding(user, stock, quantity, price);
        } else {
            BigDecimal oldValue = holding.getAveragePrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
            BigDecimal newValue = price.multiply(BigDecimal.valueOf(quantity));
            BigDecimal totalQuantity = BigDecimal.valueOf(holding.getQuantity() + quantity);
            BigDecimal newAverage = oldValue.add(newValue).divide(totalQuantity, 2, RoundingMode.HALF_UP);

            holding.setQuantity(holding.getQuantity() + quantity);
            holding.setAveragePrice(newAverage);
        }
        holdingRepository.save(holding);

        transactionRepository.save(new Transaction(user, stock, "BUY", quantity, price, LocalDateTime.now()));

        return new TradeResponse(
            "Bought " + quantity + " share(s) of " + stock.getSymbol() + ".",
            stock.getSymbol(),
            quantity,
            price,
            user.getBalance()
        );
    }

    @Transactional
    public TradeResponse sell(TradeRequest request) {
        User user = getCurrentUser();
        Stock stock = getStock(request.getSymbol());
        int quantity = validateQuantity(request.getQuantity());
        BigDecimal price = stockService.getPriceForTrade(stock.getSymbol());

        Holding holding = holdingRepository.findByUserAndStock(user, stock)
            .orElseThrow(() -> new IllegalArgumentException("You do not own shares of this stock."));

        if (holding.getQuantity() < quantity) {
            throw new IllegalArgumentException("You do not own enough shares to sell that quantity.");
        }

        BigDecimal saleAmount = price.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        int remainingQuantity = holding.getQuantity() - quantity;

        if (remainingQuantity == 0) {
            holdingRepository.delete(holding);
        } else {
            holding.setQuantity(remainingQuantity);
            holdingRepository.save(holding);
        }

        user.setBalance(user.getBalance().add(saleAmount).setScale(2, RoundingMode.HALF_UP));
        userRepository.save(user);

        transactionRepository.save(new Transaction(user, stock, "SELL", quantity, price, LocalDateTime.now()));

        return new TradeResponse(
            "Sold " + quantity + " share(s) of " + stock.getSymbol() + ".",
            stock.getSymbol(),
            quantity,
            price,
            user.getBalance()
        );
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
    }

    private Stock getStock(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Stock symbol is required.");
        }

        return stockRepository.findBySymbol(symbol.trim().toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException("Unknown stock: " + symbol));
    }

    private int validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive number.");
        }
        return quantity;
    }
}
