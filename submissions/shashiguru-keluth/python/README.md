# Modern Python 3.x Banking Client

## Overview
This is a modernized implementation of the legacy Python 2.7 banking client, upgraded to Python 3.x with modern best practices.

## Key Modernizations

### ✅ Language Modernization
- **Python 3.x syntax** with type hints
- **f-strings** for string formatting
- **Data classes** for structured data
- **Context managers** for resource management

### ✅ HTTP Client Modernization
- Modern **requests** library instead of urllib2
- **Connection pooling** for better performance
- **Retry logic** with exponential backoff
- **Timeout configuration** for reliability

### ✅ Error Handling & Logging
- **Structured logging** with logging module
- **Comprehensive exception handling**
- **Meaningful error messages**
- **Log file** for debugging

### ✅ Architecture & Design
- **SOLID principles** applied
- **Configuration management** class
- **Separation of concerns** with clear responsibilities
- **Type hints** for better code clarity

### ✅ Security & Authentication
- **JWT token management**
- **Secure credential handling**
- **Input validation** with data classes

## Setup Instructions

### Prerequisites
- Python 3.8 or higher
- pip package manager

### Installation

1. **Install dependencies**:
```bash
pip install -r requirements.txt
```

2. **Ensure the banking server is running**:
```bash
# Using Docker
docker run -d -p 8123:8123 singhacksbjb/sidequest-server:latest

# OR using Java
cd ../../server
java -jar core-banking-api.jar
```

## Usage

### Basic Usage
```bash
python banking_client.py
```

### Programmatic Usage

```python
from banking_client import BankingClient, BankingClientConfig

# Initialize client
config = BankingClientConfig(base_url="http://localhost:8123")
client = BankingClient(config)

# Simple transfer
result = client.transfer_funds("ACC1000", "ACC1001", 100.00)
if result:
    print(f"Transaction ID: {result.transaction_id}")

# Transfer with authentication
client.authenticate("testuser", "password")
result = client.transfer_funds("ACC1002", "ACC1003", 250.50, use_auth=True)

# Validate account
is_valid = client.validate_account("ACC1000")

# Get all accounts
accounts = client.get_accounts()

# Clean up
client.close()
```

## Features Implemented

### Core Features
- ✅ Transfer funds between accounts
- ✅ JWT authentication support
- ✅ Account validation
- ✅ List all accounts

### Bonus Features
- ✅ Connection pooling
- ✅ Retry logic with exponential backoff
- ✅ Comprehensive logging
- ✅ Type hints and data validation
- ✅ Configuration management
- ✅ Structured error handling

## Code Quality Improvements

### Before (Legacy Python 2.7)
```python
# Manual JSON encoding
data = '{"fromAccount":"' + from_acc + '","toAccount":"' + to_acc + '","amount":' + str(amount) + '}'

# Old urllib2
req = urllib2.Request(url, data)
response = urllib2.urlopen(req)
print "Transfer result: " + result
```

### After (Modern Python 3.x)
```python
# Structured data with validation
request = TransferRequest(from_account, to_account, amount)

# Modern requests library with proper JSON handling
response = self.session.post(url, json=payload, timeout=self.config.timeout)
logger.info(f"Transfer successful! Transaction ID: {transfer_response.transaction_id}")
```

## Testing

Run the demo to test all features:
```bash
python banking_client.py
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
- **banking_client.log** - All levels

View logs:
```bash
tail -f banking_client.log
```

## Architecture

```
BankingClient
├── BankingClientConfig (Configuration)
├── TransferRequest (Data validation)
├── TransferResponse (Response structure)
└── Session management (Connection pooling, retries)
```

## Performance Features

- **Connection pooling**: Reuses HTTP connections
- **Retry logic**: Automatic retry with backoff for transient failures
- **Timeouts**: Configurable request timeouts
- **Async ready**: Structure allows easy async/await conversion

## Security Features

- **JWT token management**: Secure authentication
- **Input validation**: Data classes validate inputs
- **No hardcoded credentials**: Configuration-based
- **HTTPS ready**: Works with both HTTP and HTTPS

## Error Handling

All errors are:
1. **Logged** with context
2. **Handled gracefully** without crashes
3. **Returned** with meaningful messages

## Comparison with Legacy Code

| Aspect | Legacy (Python 2.7) | Modern (Python 3.x) |
|--------|---------------------|---------------------|
| HTTP Library | urllib2 | requests with session |
| String Formatting | Concatenation | f-strings |
| JSON Handling | Manual strings | Built-in json |
| Error Handling | Basic try/except | Comprehensive with logging |
| Type Safety | None | Type hints |
| Configuration | Hardcoded | Config class |
| Retries | None | Automatic with backoff |
| Logging | print statements | Structured logging |
| Data Validation | None | Data classes |

## Future Enhancements

- [ ] Add async/await support
- [ ] Implement caching for account validation
- [ ] Add unit tests with pytest
- [ ] Add transaction history retrieval
- [ ] Implement rate limiting
- [ ] Add metrics and monitoring

## License
MIT License

## Author
Created for Julius Baer Banking Challenge
