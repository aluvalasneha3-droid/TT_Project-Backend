package com.example.TT.Backend;

import com.example.TT.Backend.config.DataSeeder;
import com.example.TT.Backend.model.User;
import com.example.TT.Backend.repository.StockRepository;
import com.example.TT.Backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TtBackendApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private DataSeeder dataSeeder;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${local.server.port}")
	private int port;

	@Test
	void accountTradingAndCorsFlowsWork() throws Exception {
		String firstToken = register("first-trader", "first@example.com");
		User firstUser = userRepository.findByUsername("first-trader").orElseThrow();
		assertTrue(passwordEncoder.matches("password123", firstUser.getPasswordHash()));
		assertStatus(send("GET", "/api/portfolio", firstToken, null), 200);

		HttpResponse<String> login = send("POST", "/api/auth/login", null, Map.of(
			"username", "first-trader",
			"password", "password123"));
		assertStatus(login, 200);
		String loginToken = readJson(login).get("token").asString();

		assertStatus(send("POST", "/api/auth/login", null, Map.of(
			"username", "first-trader",
			"password", "wrong-password")), 401);

		HttpResponse<String> stocks = send("GET", "/api/stocks", null, null);
		assertStatus(stocks, 200);
		assertTrue(readJson(stocks).size() > 0);

		long seededStockCount = stockRepository.count();
		dataSeeder.run();
		assertEquals(seededStockCount, stockRepository.count());

		HttpResponse<String> corsResponse = send("OPTIONS", "/api/stocks", null, null,
			"http://localhost:5173", "GET");
		assertStatus(corsResponse, 200);
		assertEquals("http://localhost:5173", corsResponse.headers()
			.firstValue(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN).orElse(null));

		assertStatus(send("GET", "/api/portfolio", "invalid-token", null), 401);

		assertStatus(send("POST", "/api/trades/buy", loginToken,
			Map.of("symbol", "TCS", "quantity", 1)), 200);
		HttpResponse<String> portfolioAfterBuy = send("GET", "/api/portfolio", loginToken, null);
		assertStatus(portfolioAfterBuy, 200);
		assertEquals(46150.0, readJson(portfolioAfterBuy).get("balance").asDouble(), 0.0);
		assertEquals(1, readJson(portfolioAfterBuy).get("holdings").size());

		HttpResponse<String> transactionsAfterBuy = send("GET", "/api/transactions", loginToken, null);
		assertStatus(transactionsAfterBuy, 200);
		assertEquals(1, readJson(transactionsAfterBuy).size());

		assertStatus(send("POST", "/api/trades/sell", loginToken,
			Map.of("symbol", "TCS", "quantity", 1)), 200);
		HttpResponse<String> portfolioAfterSell = send("GET", "/api/portfolio", loginToken, null);
		assertStatus(portfolioAfterSell, 200);
		assertEquals(50000.0, readJson(portfolioAfterSell).get("balance").asDouble(), 0.0);
		assertEquals(0, readJson(portfolioAfterSell).get("holdings").size());
		assertEquals(2, readJson(send("GET", "/api/transactions", loginToken, null)).size());

		assertStatus(send("POST", "/api/trades/buy", loginToken,
			Map.of("symbol", "TCS", "quantity", 14)), 400);
		assertStatus(send("POST", "/api/trades/sell", loginToken,
			Map.of("symbol", "TCS", "quantity", 1)), 400);

		String secondToken = register("second-trader", "second@example.com");
		HttpResponse<String> secondPortfolio = send("GET", "/api/portfolio", secondToken, null);
		assertStatus(secondPortfolio, 200);
		assertEquals(0, readJson(secondPortfolio).get("holdings").size());

		HttpResponse<String> secondTransactions = send("GET", "/api/transactions", secondToken, null);
		assertStatus(secondTransactions, 200);
		assertEquals(0, readJson(secondTransactions).size());
	}

	private String register(String username, String email) throws Exception {
		HttpResponse<String> result = send("POST", "/api/auth/register", null, Map.of(
			"username", username,
			"email", email,
			"password", "password123"));
		assertStatus(result, 200);
		return readJson(result).get("token").asString();
	}

	private HttpResponse<String> send(String method, String path, String token, Object body) throws Exception {
		return send(method, path, token, body, null, null);
	}

	private HttpResponse<String> send(String method, String path, String token, Object body,
			String origin, String requestedMethod) throws Exception {
		HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
			.header(HttpHeaders.ACCEPT, "application/json");
		if (token != null) {
			request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
		}
		if (origin != null) {
			request.header(HttpHeaders.ORIGIN, origin);
			request.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, requestedMethod);
		}
		HttpRequest.BodyPublisher bodyPublisher = body == null
			? HttpRequest.BodyPublishers.noBody()
			: HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body));
		if (body != null) {
			request.header(HttpHeaders.CONTENT_TYPE, "application/json");
		}
		return HttpClient.newHttpClient().send(
			request.method(method, bodyPublisher).build(),
			HttpResponse.BodyHandlers.ofString());
	}

	private void assertStatus(HttpResponse<String> response, int expectedStatus) {
		assertEquals(expectedStatus, response.statusCode(), response.body());
	}

	private JsonNode readJson(HttpResponse<String> response) throws Exception {
		return objectMapper.readTree(response.body());
	}

}
