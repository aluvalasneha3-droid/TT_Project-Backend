package com.example.TT.Backend.controller;

import com.example.TT.Backend.dto.TradeRequest;
import com.example.TT.Backend.dto.TradeResponse;
import com.example.TT.Backend.service.TradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/trades/buy")
    public ResponseEntity<TradeResponse> buy(@RequestBody TradeRequest request) {
        return ResponseEntity.ok(tradeService.buy(request));
    }

    @PostMapping("/trades/sell")
    public ResponseEntity<TradeResponse> sell(@RequestBody TradeRequest request) {
        return ResponseEntity.ok(tradeService.sell(request));
    }
}
