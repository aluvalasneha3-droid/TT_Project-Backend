package com.example.TT.Backend.service;

import com.example.TT.Backend.dto.StockResponse;
import com.example.TT.Backend.model.Stock;
import com.example.TT.Backend.repository.StockRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final RestClient upstoxClient;

    @Value("${marketdata.upstox.token:}")
    private String upstoxToken;

    @Value("${marketdata.upstox.instrument-keys:}")
    private String instrumentKeysJson;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
        this.upstoxClient = RestClient.builder()
            .baseUrl("https://api.upstox.com/v3")
            .build();
    }

    public List<StockResponse> getAllStocks() {
        List<Stock> stocks = stockRepository.findAll();
        Map<String, Map<String, Object>> liveQuotes = fetchLiveQuotes(stocks);
        return stocks.stream()
            .map(stock -> toResponse(stock, liveQuotes.get(stock.getSymbol())))
            .toList();
    }

    public Optional<StockResponse> getStockBySymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }
        return stockRepository.findBySymbol(symbol.trim().toUpperCase())
            .map(stock -> toResponse(stock, fetchLiveQuotes(List.of(stock)).get(stock.getSymbol())));
    }

    public BigDecimal getPriceForTrade(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol.trim().toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException("Unknown stock: " + symbol));

        if (upstoxToken == null || upstoxToken.isBlank()) {
            return stock.getCurrentPrice();
        }

        Map<String, Object> quote = fetchLiveQuotes(List.of(stock)).get(stock.getSymbol());
        if (quote == null || quote.get("last_price") == null) {
            throw new IllegalStateException("Live quote unavailable; the simulated trade was not placed.");
        }
        return decimalValue(quote.get("last_price"));
    }

    private StockResponse toResponse(Stock stock, Map<String, Object> quote) {
        StockResponse response = new StockResponse(
            stock.getId(),
            stock.getSymbol(),
            stock.getCompanyName(),
            quote != null && quote.get("last_price") != null
                ? decimalValue(quote.get("last_price"))
                : stock.getCurrentPrice()
        );

        if (quote == null) {
            response.setDataSource("DEMO");
            return response;
        }

        response.setDataSource("UPSTOX");
        if (quote.get("net_change") != null) {
            BigDecimal change = decimalValue(quote.get("net_change"));
            response.setNetChange(change);
            Object previousClose = quote.get("prev_close_price");
            if (previousClose != null && decimalValue(previousClose).signum() != 0) {
                response.setPercentChange(change
                    .multiply(BigDecimal.valueOf(100))
                    .divide(decimalValue(previousClose), 2, RoundingMode.HALF_UP));
            }
        }
        if (quote.get("timestamp") != null) {
            response.setUpdatedAt(quote.get("timestamp").toString());
        }
        return response;
    }

    private Map<String, Map<String, Object>> fetchLiveQuotes(List<Stock> stocks) {
        if (upstoxToken == null || upstoxToken.isBlank()) {
            return Map.of();
        }

        Map<String, String> instrumentKeys = readInstrumentKeys();
        Map<String, String> requestedKeys = new HashMap<>();
        for (Stock stock : stocks) {
            String instrumentKey = instrumentKeys.get(stock.getSymbol());
            if (instrumentKey != null && !instrumentKey.isBlank()) {
                requestedKeys.put(stock.getSymbol(), instrumentKey);
            }
        }
        if (requestedKeys.isEmpty()) {
            return Map.of();
        }

        try {
            String commaSeparatedKeys = String.join(",", requestedKeys.values());
            Map<String, Object> body = upstoxClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/market-quote/quotes")
                    .queryParam("instrument_key", commaSeparatedKeys)
                    .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + upstoxToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

            Object dataValue = body == null ? null : body.get("data");
            if (!(dataValue instanceof Map<?, ?> data)) {
                return Map.of();
            }

            Map<String, Map<String, Object>> quotesBySymbol = new HashMap<>();
            data.values().forEach(value -> {
                if (value instanceof Map<?, ?> quote && quote.get("symbol") != null) {
                    Map<String, Object> quoteData = new HashMap<>();
                    quote.forEach((key, field) -> quoteData.put(String.valueOf(key), field));
                    quotesBySymbol.put(String.valueOf(quote.get("symbol")).toUpperCase(), quoteData);
                }
            });
            return quotesBySymbol;
        } catch (RestClientException exception) {
            return Map.of();
        }
    }

    private Map<String, String> readInstrumentKeys() {
        if (instrumentKeysJson == null || instrumentKeysJson.isBlank()) {
            return Map.of();
        }
        Map<String, String> instrumentKeys = new HashMap<>();
        for (String item : instrumentKeysJson.split(";")) {
            int separator = item.indexOf('=');
            if (separator > 0 && separator < item.length() - 1) {
                instrumentKeys.put(
                    item.substring(0, separator).trim().toUpperCase(),
                    item.substring(separator + 1).trim()
                );
            }
        }
        return instrumentKeys;
    }

    private BigDecimal decimalValue(Object value) {
        return new BigDecimal(String.valueOf(value));
    }

    public void seedDemoStocks() {
        List<Stock> demoStocks = List.of(
            new Stock("TCS", "Tata Consultancy Services", new BigDecimal("3850.00")),
            new Stock("INFY", "Infosys", new BigDecimal("1480.00")),
            new Stock("RELIANCE", "Reliance Industries", new BigDecimal("2985.00")),
            new Stock("HDFCBANK", "HDFC Bank", new BigDecimal("1645.00")),
            new Stock("ICICIBANK", "ICICI Bank", new BigDecimal("1180.00")),
            new Stock("ITC", "ITC Limited", new BigDecimal("405.00")),
            new Stock("SBIN", "State Bank of India", new BigDecimal("810.00")),
            new Stock("BHARTIARTL", "Bharti Airtel", new BigDecimal("1940.00")),
            new Stock("LT", "Larsen & Toubro", new BigDecimal("3620.00")),
            new Stock("HINDUNILVR", "Hindustan Unilever", new BigDecimal("2580.00")),
            new Stock("KOTAKBANK", "Kotak Mahindra Bank", new BigDecimal("1880.00")),
            new Stock("AXISBANK", "Axis Bank", new BigDecimal("1190.00")),
            new Stock("BAJFINANCE", "Bajaj Finance", new BigDecimal("9150.00")),
            new Stock("MARUTI", "Maruti Suzuki India", new BigDecimal("12900.00")),
            new Stock("SUNPHARMA", "Sun Pharmaceutical Industries", new BigDecimal("1720.00")),
            new Stock("TATAMOTORS", "Tata Motors", new BigDecimal("810.00")),
            new Stock("TATASTEEL", "Tata Steel", new BigDecimal("165.00")),
            new Stock("WIPRO", "Wipro Limited", new BigDecimal("490.00")),
            new Stock("HCLTECH", "HCL Technologies", new BigDecimal("1740.00")),
            new Stock("ASIANPAINT", "Asian Paints", new BigDecimal("2920.00")),
            new Stock("ULTRACEMCO", "UltraTech Cement", new BigDecimal("11200.00")),
            new Stock("TITAN", "Titan Company", new BigDecimal("3500.00")),
            new Stock("NTPC", "NTPC Limited", new BigDecimal("370.00")),
            new Stock("POWERGRID", "Power Grid Corporation of India", new BigDecimal("325.00")),
            new Stock("ONGC", "Oil and Natural Gas Corporation", new BigDecimal("290.00")),
            new Stock("ADANIENT", "Adani Enterprises", new BigDecimal("2500.00")),
            new Stock("ADANIPORTS", "Adani Ports and Special Economic Zone", new BigDecimal("1420.00")),
            new Stock("COALINDIA", "Coal India", new BigDecimal("470.00")),
            new Stock("JSWSTEEL", "JSW Steel", new BigDecimal("1020.00")),
            new Stock("TECHM", "Tech Mahindra", new BigDecimal("1610.00")),
            new Stock("BAJAJFINSV", "Bajaj Finserv", new BigDecimal("2070.00")),
            new Stock("NESTLEIND", "Nestle India", new BigDecimal("2500.00")),
            new Stock("TRENT", "Trent Limited", new BigDecimal("8500.00")),
            new Stock("ETERNAL", "Eternal Limited", new BigDecimal("350.00")),
            new Stock("BEL", "Bharat Electronics", new BigDecimal("440.00")),
            new Stock("CIPLA", "Cipla Limited", new BigDecimal("1550.00")),
            new Stock("DRREDDY", "Dr. Reddy's Laboratories", new BigDecimal("1250.00")),
            new Stock("INDUSINDBK", "IndusInd Bank", new BigDecimal("950.00")),
            new Stock("TATACONSUM", "Tata Consumer Products", new BigDecimal("1100.00"))
        );

        demoStocks.stream()
            .filter(stock -> stockRepository.findBySymbol(stock.getSymbol()).isEmpty())
            .forEach(stockRepository::save);
    }
}
