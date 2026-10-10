package com.example.TT.Backend.service;

import com.example.TT.Backend.dto.TransactionResponse;
import com.example.TT.Backend.model.Transaction;
import com.example.TT.Backend.model.User;
import com.example.TT.Backend.repository.TransactionRepository;
import com.example.TT.Backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public List<TransactionResponse> getTransactions() {
        User user = getCurrentUser();
        return transactionRepository.findByUserOrderByCreatedAtDesc(user).stream()
            .map(this::toResponse)
            .toList();
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getStock().getSymbol(),
            transaction.getType(),
            transaction.getQuantity(),
            transaction.getPrice(),
            transaction.getCreatedAt()
        );
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
    }
}
