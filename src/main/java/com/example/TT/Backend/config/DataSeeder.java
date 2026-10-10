package com.example.TT.Backend.config;

import com.example.TT.Backend.service.StockService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final StockService stockService;

    public DataSeeder(StockService stockService) {
        this.stockService = stockService;
    }

    @Override
    public void run(String... args) {
        stockService.seedDemoStocks();
    }
}
