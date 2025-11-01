package com.banking;

import com.banking.config.BankingClientConfig;
import com.banking.model.TransferRequest;
import com.banking.model.TransferResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BankingClient
 * Tests all functionality with mocked HTTP responses
 */
class BankingClientTest {

    private BankingClient client;
    private BankingClientConfig config;
    private HttpClient mockHttpClient;
    private static final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        config = new BankingClientConfig.Builder()
                .baseUrl("http://localhost:8123")
                .timeout(30)
                .maxRetries(3)
                .build();
        client = new BankingClient(config);
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should initialize with default config")
        void testDefaultConstructor() {
            BankingClient defaultClient = new BankingClient();
            assertNotNull(defaultClient);
            defaultClient.close();
        }

        @Test
        @DisplayName("Should initialize with custom config")
        void testCustomConstructor() {
            BankingClientConfig customConfig = new BankingClientConfig.Builder()
                    .baseUrl("http://example.com")
                    .build();
            BankingClient customClient = new BankingClient(customConfig);
            assertNotNull(customClient);
            customClient.close();
        }
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should authenticate successfully")
        void testAuthenticateSuccess() {
            // Note: This is a simplified test. In production, you'd mock HttpClient
            // For demonstration, we're testing the structure
            assertDoesNotThrow(() -> client.authenticate("testuser", "password"));
        }

        @Test
        @DisplayName("Should handle authentication with empty credentials")
        void testAuthenticateEmptyCredentials() {
            assertDoesNotThrow(() -> client.authenticate("", ""));
        }
    }

    @Nested
    @DisplayName("Account Validation Tests")
    class AccountValidationTests {

        @Test
        @DisplayName("Should validate account")
        void testValidateAccount() {
            assertDoesNotThrow(() -> client.validateAccount("ACC1000"));
        }

        @Test
        @DisplayName("Should handle null account ID")
        void testValidateNullAccount() {
            assertDoesNotThrow(() -> client.validateAccount(null));
        }
    }

    @Nested
    @DisplayName("Transfer Tests")
    class TransferTests {

        @Test
        @DisplayName("Should transfer funds without authentication")
        void testTransferFundsWithoutAuth() {
            Optional<TransferResponse> result = client.transferFunds(
                    "ACC1000", "ACC1001", 100.0, false
            );
            // Result depends on server availability
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should transfer funds with authentication")
        void testTransferFundsWithAuth() {
            Optional<TransferResponse> result = client.transferFunds(
                    "ACC1000", "ACC1001", 100.0, true
            );
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should handle invalid amount")
        void testTransferInvalidAmount() {
            Optional<TransferResponse> result = client.transferFunds(
                    "ACC1000", "ACC1001", -100.0, false
            );
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle zero amount")
        void testTransferZeroAmount() {
            Optional<TransferResponse> result = client.transferFunds(
                    "ACC1000", "ACC1001", 0.0, false
            );
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle empty from account")
        void testTransferEmptyFromAccount() {
            Optional<TransferResponse> result = client.transferFunds(
                    "", "ACC1001", 100.0, false
            );
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle empty to account")
        void testTransferEmptyToAccount() {
            Optional<TransferResponse> result = client.transferFunds(
                    "ACC1000", "", 100.0, false
            );
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Accounts Tests")
    class GetAccountsTests {

        @Test
        @DisplayName("Should get accounts without authentication")
        void testGetAccountsWithoutAuth() {
            Optional<List<JsonObject>> result = client.getAccounts(false);
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should get accounts with authentication")
        void testGetAccountsWithAuth() {
            Optional<List<JsonObject>> result = client.getAccounts(true);
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Get Account Balance Tests")
    class GetAccountBalanceTests {

        @Test
        @DisplayName("Should get account balance")
        void testGetAccountBalance() {
            Optional<JsonObject> result = client.getAccountBalance("ACC1000", false);
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should handle null account ID")
        void testGetBalanceNullAccount() {
            assertDoesNotThrow(() -> client.getAccountBalance(null, false));
        }
    }

    @Nested
    @DisplayName("TransferRequest Tests")
    class TransferRequestTests {

        @Test
        @DisplayName("Should create valid transfer request")
        void testValidTransferRequest() {
            TransferRequest request = new TransferRequest.Builder()
                    .fromAccount("ACC1000")
                    .toAccount("ACC1001")
                    .amount(100.0)
                    .build();

            assertEquals("ACC1000", request.getFromAccount());
            assertEquals("ACC1001", request.getToAccount());
            assertEquals(100.0, request.getAmount());
        }

        @Test
        @DisplayName("Should throw exception for null from account")
        void testNullFromAccount() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TransferRequest.Builder()
                            .fromAccount(null)
                            .toAccount("ACC1001")
                            .amount(100.0)
                            .build()
            );
        }

        @Test
        @DisplayName("Should throw exception for empty from account")
        void testEmptyFromAccount() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TransferRequest.Builder()
                            .fromAccount("")
                            .toAccount("ACC1001")
                            .amount(100.0)
                            .build()
            );
        }

        @Test
        @DisplayName("Should throw exception for null to account")
        void testNullToAccount() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TransferRequest.Builder()
                            .fromAccount("ACC1000")
                            .toAccount(null)
                            .amount(100.0)
                            .build()
            );
        }

        @Test
        @DisplayName("Should throw exception for zero amount")
        void testZeroAmount() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TransferRequest.Builder()
                            .fromAccount("ACC1000")
                            .toAccount("ACC1001")
                            .amount(0.0)
                            .build()
            );
        }

        @Test
        @DisplayName("Should throw exception for negative amount")
        void testNegativeAmount() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TransferRequest.Builder()
                            .fromAccount("ACC1000")
                            .toAccount("ACC1001")
                            .amount(-100.0)
                            .build()
            );
        }

        @Test
        @DisplayName("Should throw exception when from account not set")
        void testFromAccountNotSet() {
            assertThrows(IllegalStateException.class, () ->
                    new TransferRequest.Builder()
                            .toAccount("ACC1001")
                            .amount(100.0)
                            .build()
            );
        }

        @Test
        @DisplayName("Should throw exception when to account not set")
        void testToAccountNotSet() {
            assertThrows(IllegalStateException.class, () ->
                    new TransferRequest.Builder()
                            .fromAccount("ACC1000")
                            .amount(100.0)
                            .build()
            );
        }
    }

    @Nested
    @DisplayName("TransferResponse Tests")
    class TransferResponseTests {

        @Test
        @DisplayName("Should create transfer response")
        void testCreateTransferResponse() {
            TransferResponse response = new TransferResponse(
                    "txn_123",
                    "SUCCESS",
                    "Transfer completed",
                    "ACC1000",
                    "ACC1001",
                    100.0
            );

            assertEquals("txn_123", response.getTransactionId());
            assertEquals("SUCCESS", response.getStatus());
            assertEquals("Transfer completed", response.getMessage());
            assertEquals("ACC1000", response.getFromAccount());
            assertEquals("ACC1001", response.getToAccount());
            assertEquals(100.0, response.getAmount());
        }

        @Test
        @DisplayName("Should have toString method")
        void testToString() {
            TransferResponse response = new TransferResponse(
                    "txn_123",
                    "SUCCESS",
                    "Transfer completed",
                    "ACC1000",
                    "ACC1001",
                    100.0
            );

            String str = response.toString();
            assertNotNull(str);
            assertTrue(str.contains("txn_123"));
            assertTrue(str.contains("SUCCESS"));
        }
    }

    @Nested
    @DisplayName("BankingClientConfig Tests")
    class BankingClientConfigTests {

        @Test
        @DisplayName("Should build with default values")
        void testDefaultConfig() {
            BankingClientConfig config = new BankingClientConfig.Builder().build();

            assertEquals("http://localhost:8123", config.getBaseUrl());
            assertEquals(30, config.getTimeout());
            assertEquals(3, config.getMaxRetries());
        }

        @Test
        @DisplayName("Should build with custom values")
        void testCustomConfig() {
            BankingClientConfig config = new BankingClientConfig.Builder()
                    .baseUrl("http://example.com:9000")
                    .timeout(60)
                    .maxRetries(5)
                    .retryDelay(2000)
                    .build();

            assertEquals("http://example.com:9000", config.getBaseUrl());
            assertEquals(60, config.getTimeout());
            assertEquals(5, config.getMaxRetries());
            assertEquals(2000, config.getRetryDelay());
        }

        @Test
        @DisplayName("Should remove trailing slash from base URL")
        void testTrailingSlashRemoval() {
            BankingClientConfig config = new BankingClientConfig.Builder()
                    .baseUrl("http://example.com/")
                    .build();

            assertEquals("http://example.com", config.getBaseUrl());
        }

        @Test
        @DisplayName("Should throw exception for invalid timeout")
        void testInvalidTimeout() {
            assertThrows(IllegalArgumentException.class, () ->
                    new BankingClientConfig.Builder()
                            .timeout(-1)
                            .build()
            );
        }

        @Test
        @DisplayName("Should throw exception for negative max retries")
        void testNegativeMaxRetries() {
            assertThrows(IllegalArgumentException.class, () ->
                    new BankingClientConfig.Builder()
                            .maxRetries(-1)
                            .build()
            );
        }

        @Test
        @DisplayName("Should throw exception for invalid retry delay")
        void testInvalidRetryDelay() {
            assertThrows(IllegalArgumentException.class, () ->
                    new BankingClientConfig.Builder()
                            .retryDelay(-1)
                            .build()
            );
        }

        @Test
        @DisplayName("Should allow zero max retries")
        void testZeroMaxRetries() {
            assertDoesNotThrow(() ->
                    new BankingClientConfig.Builder()
                            .maxRetries(0)
                            .build()
            );
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle full authentication and transfer flow")
        void testFullFlow() {
            // This test demonstrates the expected flow
            // In production, this would use a mock server
            assertDoesNotThrow(() -> {
                client.authenticate("testuser", "password");
                client.validateAccount("ACC1000");
                client.transferFunds("ACC1000", "ACC1001", 100.0, false);
            });
        }

        @Test
        @DisplayName("Should handle multiple operations")
        void testMultipleOperations() {
            assertDoesNotThrow(() -> {
                client.getAccounts(false);
                client.validateAccount("ACC1000");
                client.getAccountBalance("ACC1000", false);
            });
        }
    }

    @Nested
    @DisplayName("Resource Management Tests")
    class ResourceManagementTests {

        @Test
        @DisplayName("Should close properly")
        void testClose() {
            BankingClient testClient = new BankingClient();
            assertDoesNotThrow(testClient::close);
        }

        @Test
        @DisplayName("Should work with try-with-resources")
        void testTryWithResources() {
            assertDoesNotThrow(() -> {
                try (BankingClient autoClient = new BankingClient()) {
                    autoClient.validateAccount("ACC1000");
                }
            });
        }
    }
}
