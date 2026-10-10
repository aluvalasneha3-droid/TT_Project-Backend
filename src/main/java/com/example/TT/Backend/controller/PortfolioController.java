package com.example.TT.Backend.controller;

import com.example.TT.Backend.dto.PortfolioResponse;
import com.example.TT.Backend.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/portfolio")
    public ResponseEntity<PortfolioResponse> getPortfolio() {
        return ResponseEntity.ok(portfolioService.getPortfolio());
    }
}
