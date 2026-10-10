package com.example.TT.Backend.controller;

import com.example.TT.Backend.dto.TransactionResponse;
import com.example.TT.Backend.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions() {
        return ResponseEntity.ok(transactionService.getTransactions());
    }
}
