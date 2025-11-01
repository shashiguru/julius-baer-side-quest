package com.banking;

import com.banking.config.BankingClientConfig;
import com.banking.model.TransferRequest;
import com.banking.model.TransferResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Modern Java 11+ Banking Client
 * ================================
 * A modernized implementation of the legacy Java 6 banking client with:
 * - Java 11+ features (var keyword, HTTP Client API, modern syntax)
 * - Modern JSON libraries (Gson for serialization)
 * - Comprehensive error handling with custom exceptions
 * - SLF4J logging framework
 * - Builder patterns and immutable objects
 * - Retry logic and timeout configuration
 */
public class BankingClient implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(BankingClient.class);
    private static final Gson gson = new Gson();
    
    private final BankingClientConfig config;
    private final HttpClient httpClient;
    private String jwtToken;
    
    /**
     * Constructor with default configuration
     */
    public BankingClient() {
        this(new BankingClientConfig.Builder().build());
    }
    
    /**
     * Constructor with custom configuration
     * @param config Configuration object
     */
    public BankingClient(BankingClientConfig config) {
        this.config = config;
        this.httpClient = createHttpClient();
        logger.info("Banking client initialized with base URL: {}", config.getBaseUrl());
    }
    
    /**
     * Create HTTP client with modern Java 11+ API
     */
    private HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(config.getTimeout()))
                .build();
    }
    
    /**
     * Authenticate with the banking API
     * @param username Username for authentication
     * @param password Password for authentication
     * @return true if authentication successful
     */
    public boolean authenticate(String username, String password) {
        var url = config.getBaseUrl() + "/authToken";
        
        try {
            logger.info("Attempting authentication for user: {}", username);
            
            var payload = new JsonObject();
            payload.addProperty("username", username);
            payload.addProperty("password", password);
            
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(config.getTimeout()))
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                    .build();
            
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                var jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                this.jwtToken = jsonResponse.get("token").getAsString();
                logger.info("Authentication successful");
                return true;
            } else {
                logger.error("Authentication failed with status: {}", response.statusCode());
                return false;
            }
        } catch (Exception e) {
            logger.error("Authentication failed: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Validate if an account exists and is active
     * @param accountId Account ID to validate
     * @return true if account is valid
     */
    public boolean validateAccount(String accountId) {
        var url = config.getBaseUrl() + "/accounts/validate/" + accountId;
        
        try {
            logger.info("Validating account: {}", accountId);
            
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(config.getTimeout()))
                    .GET()
                    .build();
            
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                var jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                var isValid = jsonResponse.get("valid").getAsBoolean();
                logger.info("Account {} validation result: {}", accountId, isValid);
                return isValid;
            } else {
                logger.error("Account validation failed with status: {}", response.statusCode());
                return false;
            }
        } catch (Exception e) {
            logger.error("Account validation failed: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Transfer funds between accounts with retry logic
     * @param fromAccount Source account ID
     * @param toAccount Destination account ID
     * @param amount Amount to transfer
     * @param useAuth Whether to use JWT authentication
     * @return TransferResponse object if successful, empty Optional otherwise
     */
    public Optional<TransferResponse> transferFunds(
            String fromAccount, 
            String toAccount, 
            double amount,
            boolean useAuth) {
        
        return transferFundsWithRetry(fromAccount, toAccount, amount, useAuth, config.getMaxRetries());
    }
    
    /**
     * Transfer funds with retry logic
     */
    private Optional<TransferResponse> transferFundsWithRetry(
            String fromAccount,
            String toAccount,
            double amount,
            boolean useAuth,
            int retriesLeft) {
        
        var url = config.getBaseUrl() + "/transfer";
        
        try {
            // Validate and create request
            var transferRequest = new TransferRequest.Builder()
                    .fromAccount(fromAccount)
                    .toAccount(toAccount)
                    .amount(amount)
                    .build();
            
            logger.info("Initiating transfer: {} -> {}, Amount: ${}", 
                    transferRequest.getFromAccount(), 
                    transferRequest.getToAccount(), 
                    transferRequest.getAmount());
            
            // Build HTTP request
            var requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(config.getTimeout()))
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(transferRequest)));
            
            // Add JWT token if authentication is enabled
            if (useAuth && jwtToken != null) {
                requestBuilder.header("Authorization", "Bearer " + jwtToken);
            }
            
            var request = requestBuilder.build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                var transferResponse = gson.fromJson(response.body(), TransferResponse.class);
                logger.info("Transfer successful! Transaction ID: {}, Status: {}", 
                        transferResponse.getTransactionId(), 
                        transferResponse.getStatus());
                return Optional.of(transferResponse);
            } else {
                logger.error("Transfer failed with status: {} - {}", 
                        response.statusCode(), 
                        response.body());
                return Optional.empty();
            }
        } catch (IOException | InterruptedException e) {
            if (retriesLeft > 0) {
                logger.warn("Request failed, retrying... ({} retries left)", retriesLeft);
                try {
                    Thread.sleep(config.getRetryDelay() * (config.getMaxRetries() - retriesLeft + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return transferFundsWithRetry(fromAccount, toAccount, amount, useAuth, retriesLeft - 1);
            }
            logger.error("Transfer failed after all retries: {}", e.getMessage(), e);
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            logger.error("Invalid transfer request: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * Retrieve list of all accounts
     * @param useAuth Whether to use JWT authentication
     * @return List of accounts
     */
    public Optional<List<JsonObject>> getAccounts(boolean useAuth) {
        var url = config.getBaseUrl() + "/accounts";
        
        try {
            logger.info("Fetching accounts list");
            
            var requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(config.getTimeout()))
                    .GET();
            
            if (useAuth && jwtToken != null) {
                requestBuilder.header("Authorization", "Bearer " + jwtToken);
            }
            
            var request = requestBuilder.build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                var accounts = gson.fromJson(response.body(), List.class);
                logger.info("Retrieved {} accounts", accounts.size());
                return Optional.of(accounts);
            } else {
                logger.error("Failed to retrieve accounts with status: {}", response.statusCode());
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve accounts: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * Get account balance
     * @param accountId Account ID
     * @param useAuth Whether to use JWT authentication
     * @return Balance information
     */
    public Optional<JsonObject> getAccountBalance(String accountId, boolean useAuth) {
        var url = config.getBaseUrl() + "/accounts/balance/" + accountId;
        
        try {
            logger.info("Fetching balance for account: {}", accountId);
            
            var requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(config.getTimeout()))
                    .GET();
            
            if (useAuth && jwtToken != null) {
                requestBuilder.header("Authorization", "Bearer " + jwtToken);
            }
            
            var request = requestBuilder.build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                var balanceData = gson.fromJson(response.body(), JsonObject.class);
                logger.info("Balance retrieved for {}: ${}", accountId, balanceData.get("balance"));
                return Optional.of(balanceData);
            } else {
                logger.error("Failed to get balance with status: {}", response.statusCode());
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve balance: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public void close() {
        logger.info("Banking client closed");
    }
    
    /**
     * Main demonstration method
     */
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("Modern Banking Client - Java 11+ Implementation");
        System.out.println("=".repeat(60));
        
        var config = new BankingClientConfig.Builder()
                .baseUrl("http://localhost:8123")
                .timeout(30)
                .maxRetries(3)
                .build();
        
        try (var client = new BankingClient(config)) {
            
            // Example 1: Basic transfer without authentication
            System.out.println("\n[1] Basic Transfer (No Authentication)");
            System.out.println("-".repeat(60));
            var result1 = client.transferFunds("ACC1000", "ACC1001", 100.00, false);
            result1.ifPresentOrElse(
                    response -> {
                        System.out.println("✓ Transaction ID: " + response.getTransactionId());
                        System.out.println("✓ Status: " + response.getStatus());
                        System.out.println("✓ Message: " + response.getMessage());
                    },
                    () -> System.out.println("✗ Transfer failed")
            );
            
            // Example 2: Transfer with authentication
            System.out.println("\n[2] Transfer with JWT Authentication");
            System.out.println("-".repeat(60));
            if (client.authenticate("testuser", "password")) {
                System.out.println("✓ Authentication successful");
                var result2 = client.transferFunds("ACC1002", "ACC1003", 250.50, true);
                result2.ifPresent(response -> {
                    System.out.println("✓ Transaction ID: " + response.getTransactionId());
                    System.out.println("✓ Status: " + response.getStatus());
                });
            } else {
                System.out.println("✗ Authentication failed");
            }
            
            // Example 3: Account validation
            System.out.println("\n[3] Account Validation");
            System.out.println("-".repeat(60));
            String[] accountsToValidate = {"ACC1000", "ACC2000", "ACC9999"};
            for (String acc : accountsToValidate) {
                boolean isValid = client.validateAccount(acc);
                String status = isValid ? "✓ Valid" : "✗ Invalid";
                System.out.println(status + ": " + acc);
            }
            
            // Example 4: Get all accounts
            System.out.println("\n[4] Retrieve All Accounts");
            System.out.println("-".repeat(60));
            var accounts = client.getAccounts(false);
            accounts.ifPresentOrElse(
                    list -> {
                        System.out.println("✓ Found " + list.size() + " accounts");
                        list.stream()
                                .limit(5)
                                .forEach(acc -> System.out.println("  - " + acc));
                    },
                    () -> System.out.println("✗ Failed to retrieve accounts")
            );
            
            // Example 5: Error handling - Invalid transfer
            System.out.println("\n[5] Error Handling Demo (Invalid Account)");
            System.out.println("-".repeat(60));
            var result5 = client.transferFunds("ACC9999", "ACC1001", 50.00, false);
            if (result5.isEmpty()) {
                System.out.println("✓ Error handled gracefully - check logs for details");
            }
            
        } catch (Exception e) {
            logger.error("Demo failed", e);
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Demo completed!");
        System.out.println("=".repeat(60));
    }
}
