# Modern Java 11+ Banking Client

## Overview
This is a modernized implementation of the legacy Java 6 banking client, upgraded to Java 11+ with modern best practices.

## Key Modernizations

### ✅ Language Modernization
- **Java 11+ features** including var keyword
- **Modern HTTP Client API** (java.net.http)
- **Optional** for null safety
- **Try-with-resources** for resource management
- **Lambda expressions** and streams

### ✅ HTTP Client Modernization
- Modern **java.net.http.HttpClient** instead of HttpURLConnection
- **Connection pooling** and timeout configuration
- **HTTP/2 support** with fallback to HTTP/1.1
- **Proper resource management**

### ✅ Error Handling & Logging
- **SLF4J** logging framework with Logback
- **Structured exception handling**
- **Meaningful error messages** with context
- **Log file** and console output

### ✅ Architecture & Design
- **Builder pattern** for immutable objects
- **SOLID principles** applied
- **Dependency injection** ready
- **Separation of concerns** with packages
- **AutoCloseable** for resource management

### ✅ JSON & Data Handling
- **Gson** for modern JSON serialization
- **Type-safe models** with validation
- **Immutable objects** for thread safety

## Project Structure

```
java/
├── pom.xml                                    # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/com/banking/
│   │   │   ├── BankingClient.java           # Main client class
│   │   │   ├── config/
│   │   │   │   └── BankingClientConfig.java # Configuration
│   │   │   └── model/
│   │   │       ├── TransferRequest.java     # Request model
│   │   │       └── TransferResponse.java    # Response model
│   │   └── resources/
│   │       └── logback.xml                   # Logging configuration
│   └── test/
│       └── java/com/banking/                # Test classes
└── README.md                                 # This file
```

## Setup Instructions

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

### Check Java Version
```bash
java -version
# Should show Java 11 or higher
```

### Installation

1. **Navigate to the Java directory**:
```bash
cd submissions/modernized-solutions/java
```

2. **Build the project**:
```bash
mvn clean package
```

3. **Ensure the banking server is running**:
```bash
# Using Docker
docker run -d -p 8123:8123 singhacksbjb/sidequest-server:latest

# OR using Java
cd ../../../server
java -jar core-banking-api.jar
```

## Usage

### Run with Maven
```bash
mvn exec:java -Dexec.mainClass="com.banking.BankingClient"
```

### Run Fat JAR
```bash
java -jar target/banking-client-modern-1.0.0-fat.jar
```

### Programmatic Usage

```java
import com.banking.BankingClient;
import com.banking.config.BankingClientConfig;

public class Demo {
    public static void main(String[] args) {
        // Create configuration
        var config = new BankingClientConfig.Builder()
                .baseUrl("http://localhost:8123")
                .timeout(30)
                .maxRetries(3)
                .build();
        
        // Create client with try-with-resources
        try (var client = new BankingClient(config)) {
            
            // Simple transfer
            var result = client.transferFunds("ACC1000", "ACC1001", 100.00, false);
            result.ifPresent(response -> {
                System.out.println("Transaction ID: " + response.getTransactionId());
            });
            
            // Transfer with authentication
            client.authenticate("testuser", "password");
            var authResult = client.transferFunds("ACC1002", "ACC1003", 250.50, true);
            
            // Validate account
            boolean isValid = client.validateAccount("ACC1000");
            
            // Get all accounts
            var accounts = client.getAccounts(false);
            accounts.ifPresent(list -> {
                System.out.println("Found " + list.size() + " accounts");
            });
        }
    }
}
```

## Features Implemented

### Core Features
- ✅ Transfer funds between accounts
- ✅ JWT authentication support
- ✅ Account validation
- ✅ List all accounts
- ✅ Get account balance

### Bonus Features
- ✅ Builder pattern for configuration
- ✅ Retry logic with exponential backoff
- ✅ Comprehensive logging (SLF4J + Logback)
- ✅ Type-safe models with validation
- ✅ Optional for null safety
- ✅ Immutable objects
- ✅ AutoCloseable resource management
- ✅ Modern HTTP Client API

## Code Quality Improvements

### Before (Legacy Java 6)
```java
// Manual JSON string building
String jsonData = "{\"fromAccount\":\"" + fromAccount + 
                 "\",\"toAccount\":\"" + toAccount + 
                 "\",\"amount\":" + amount + "}";

// Old HttpURLConnection
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setDoOutput(true);
OutputStream os = conn.getOutputStream();
os.write(jsonData.getBytes());

System.out.println("Response: " + response.toString());
```

### After (Modern Java 11+)
```java
// Type-safe request with validation
var transferRequest = new TransferRequest.Builder()
        .fromAccount(fromAccount)
        .toAccount(toAccount)
        .amount(amount)
        .build();

// Modern HTTP Client
var request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(transferRequest)))
        .build();
var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

logger.info("Transfer successful! Transaction ID: {}", transferResponse.getTransactionId());
```

## Build Commands

### Compile
```bash
mvn compile
```

### Package
```bash
mvn package
```

### Clean and Package
```bash
mvn clean package
```

### Run Tests
```bash
mvn test
```

## Testing

Run the demo to test all features:
```bash
java -jar target/banking-client-modern-1.0.0-fat.jar
```

Expected output:
```
[1] Basic Transfer (No Authentication)
✓ Transaction ID: abc123...
✓ Status: SUCCESS

[2] Transfer with JWT Authentication
✓ Authentication successful
✓ Transaction ID: def456...

[3] Account Validation
✓ Valid: ACC1000
✗ Invalid: ACC2000

[4] Retrieve All Accounts
✓ Found 100 accounts

[5] Error Handling Demo
✓ Error handled gracefully
```

## Logging

Logs are written to:
- **Console** (stdout) - INFO level
- **banking-client.log** - DEBUG level

View logs:
```bash
tail -f banking-client.log
```

Configure logging in `src/main/resources/logback.xml`

## Architecture

```
BankingClient (Main client)
├── BankingClientConfig (Configuration with Builder)
├── TransferRequest (Request model with Builder)
├── TransferResponse (Response model)
└── HttpClient (Java 11+ HTTP Client)
```

## Design Patterns Used

1. **Builder Pattern**: For flexible object creation (Config, Request)
2. **Factory Pattern**: HTTP client creation
3. **Resource Management**: AutoCloseable/try-with-resources
4. **Immutability**: Final fields, no setters
5. **Dependency Injection Ready**: Constructor injection
6. **Optional Pattern**: Null safety

## Performance Features

- **Connection pooling**: Managed by HttpClient
- **Retry logic**: Automatic retry with exponential backoff
- **Timeouts**: Configurable connection and request timeouts
- **HTTP/2 support**: Modern protocol with multiplexing

## Security Features

- **JWT token management**: Secure authentication
- **Input validation**: Builder validation
- **No hardcoded credentials**: Configuration-based
- **HTTPS ready**: Works with both HTTP and HTTPS
- **Secure logging**: No sensitive data in logs

## Error Handling

All errors are:
1. **Caught** at appropriate levels
2. **Logged** with SLF4J
3. **Wrapped** in Optional for null safety
4. **Handled gracefully** without crashes

## Comparison with Legacy Code

| Aspect | Legacy (Java 6) | Modern (Java 11+) |
|--------|-----------------|-------------------|
| HTTP Library | HttpURLConnection | java.net.http.HttpClient |
| JSON Handling | Manual string concat | Gson library |
| Error Handling | Basic try/catch | Comprehensive + logging |
| Type Safety | StringBuffer | Immutable models |
| Null Safety | null checks | Optional |
| Resource Management | Manual close | Try-with-resources |
| Configuration | Hardcoded | Builder pattern |
| Retries | None | Automatic with backoff |
| Logging | System.out | SLF4J + Logback |
| Validation | None | Builder validation |
| Language Features | Java 6 | var, lambdas, streams |

## Dependencies

- **Gson**: 2.10.1 - JSON serialization
- **SLF4J**: 2.0.9 - Logging API
- **Logback**: 1.4.11 - Logging implementation
- **JUnit**: 5.10.0 - Testing framework

## Maven Plugins

- **maven-compiler-plugin**: Compile with Java 11
- **maven-jar-plugin**: Create JAR with manifest
- **maven-shade-plugin**: Create fat JAR with dependencies
- **maven-surefire-plugin**: Run tests

## Future Enhancements

- [ ] Add JUnit 5 unit tests
- [ ] Implement reactive streams with Flow API
- [ ] Add caching for account validation
- [ ] Implement transaction history retrieval
- [ ] Add rate limiting
- [ ] Implement connection pool tuning
- [ ] Add metrics with Micrometer
- [ ] Create Spring Boot integration

## IDE Setup

### IntelliJ IDEA
1. Import as Maven project
2. Set Project SDK to Java 11+
3. Enable annotation processing
4. Run BankingClient.main()

### Eclipse
1. Import as Existing Maven Project
2. Update project configuration
3. Set Java compliance to 11
4. Run as Java Application

### VS Code
1. Install Java Extension Pack
2. Open folder
3. Maven will auto-configure
4. Run from Run menu

## License
MIT License

## Author
Created for Julius Baer Banking Challenge
