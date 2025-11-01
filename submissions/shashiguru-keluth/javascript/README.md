# Modern JavaScript ES6+ Banking Client

## Overview
This is a modernized implementation of the legacy ES5 banking client, upgraded to ES6+ with modern JavaScript best practices.

## Key Modernizations

### ✅ Language Modernization
- **ES6+ syntax** with const/let instead of var
- **Arrow functions** and modern class syntax
- **Template literals** for string interpolation
- **Destructuring** and spread operators
- **Async/await** pattern for asynchronous operations

### ✅ HTTP Client Modernization
- Modern **Fetch API** instead of XMLHttpRequest
- **Asynchronous operations** (no more synchronous blocking)
- **Promise-based** architecture
- **Timeout support** with AbortController
- **Retry logic** with exponential backoff

### ✅ Error Handling & Logging
- **Structured logging** with Logger class
- **Comprehensive try/catch** blocks
- **Meaningful error messages** with context
- **Colored console output** for better visibility

### ✅ Architecture & Design
- **Class-based architecture** with clear separation
- **Configuration management** class
- **Input validation** with dedicated classes
- **Reusable utility methods**

### ✅ Security & Authentication
- **JWT token management**
- **Input sanitization** and validation
- **Secure header handling**

## Setup Instructions

### Prerequisites
- Node.js 14.0 or higher
- npm package manager

### Installation

1. **Install dependencies**:
```bash
npm install
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

### Run Demo
```bash
npm start
# or
node bankingClient.js
```

### Programmatic Usage

```javascript
import { BankingClient, BankingClientConfig } from './bankingClient.js';

// Initialize client
const config = new BankingClientConfig({ baseUrl: 'http://localhost:8123' });
const client = new BankingClient(config);

// Simple transfer
const result = await client.transferFunds('ACC1000', 'ACC1001', 100.00);
if (result) {
  console.log(`Transaction ID: ${result.transactionId}`);
}

// Transfer with authentication
await client.authenticate('testuser', 'password');
const authResult = await client.transferFunds('ACC1002', 'ACC1003', 250.50, true);

// Validate account
const isValid = await client.validateAccount('ACC1000');

// Get all accounts
const accounts = await client.getAccounts();

// Get account balance
const balance = await client.getAccountBalance('ACC1000');
```

### Browser Usage

For browser environments, you can use the client directly:

```html
<!DOCTYPE html>
<html>
<head>
    <title>Banking Client Demo</title>
</head>
<body>
    <script type="module">
        import { BankingClient, BankingClientConfig } from './bankingClient.js';
        
        const client = new BankingClient();
        
        async function transfer() {
            const result = await client.transferFunds('ACC1000', 'ACC1001', 100);
            console.log(result);
        }
        
        transfer();
    </script>
</body>
</html>
```

## Features Implemented

### Core Features
- ✅ Transfer funds between accounts
- ✅ JWT authentication support
- ✅ Account validation
- ✅ List all accounts
- ✅ Get account balance

### Bonus Features
- ✅ Timeout support with AbortController
- ✅ Retry logic with exponential backoff
- ✅ Structured logging with colored output
- ✅ Input validation and sanitization
- ✅ Configuration management
- ✅ Comprehensive error handling
- ✅ Promise-based async operations

## Code Quality Improvements

### Before (Legacy ES5)
```javascript
// Old XMLHttpRequest approach
var xhr = new XMLHttpRequest();
var data = '{"fromAccount":"' + fromAccount + '","toAccount":"' + toAccount + '","amount":' + amount + '}';
xhr.open("POST", url, false); // Synchronous - BAD!
xhr.send(data);
console.log("Transfer successful: " + result.transactionId);
```

### After (Modern ES6+)
```javascript
// Modern fetch API with async/await
const request = new TransferRequest(fromAccount, toAccount, amount);
const response = await this._fetchWithRetry(url, {
  method: 'POST',
  headers: this._getHeaders(),
  body: JSON.stringify(request.toJSON())
});
Logger.success(`Transfer successful! Transaction ID: ${data.transactionId}`);
```

## Testing

Run the demo to test all features:
```bash
npm start
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

## Architecture

```
BankingClient
├── BankingClientConfig (Configuration)
├── TransferRequest (Data validation)
├── Logger (Structured logging)
└── Private methods
    ├── _fetchWithTimeout (Timeout support)
    ├── _fetchWithRetry (Retry logic)
    └── _getHeaders (Header management)
```

## Performance Features

- **Timeout support**: Configurable request timeouts with AbortController
- **Retry logic**: Automatic retry with exponential backoff for transient failures
- **Async operations**: Non-blocking asynchronous requests
- **Connection reuse**: Modern fetch API automatically manages connections

## Security Features

- **JWT token management**: Secure authentication with Bearer tokens
- **Input validation**: TransferRequest class validates all inputs
- **No hardcoded credentials**: Configuration-based
- **HTTPS ready**: Works with both HTTP and HTTPS
- **Error message sanitization**: No sensitive data in logs

## Error Handling

All errors are:
1. **Caught** at appropriate levels
2. **Logged** with structured context
3. **Handled gracefully** without crashes
4. **Returned** with meaningful messages

## Comparison with Legacy Code

| Aspect | Legacy (ES5) | Modern (ES6+) |
|--------|--------------|---------------|
| HTTP Library | XMLHttpRequest | Fetch API |
| Async Pattern | Callbacks/Synchronous | Async/await |
| String Formatting | Concatenation | Template literals |
| Variables | var | const/let |
| Functions | function() {} | Arrow functions |
| Classes | Constructor functions | ES6 classes |
| Error Handling | Basic try/catch | Comprehensive with logging |
| Configuration | Hardcoded | Config class |
| Retries | None | Automatic with backoff |
| Timeout | None | AbortController |
| Logging | console.log | Structured Logger |
| Validation | None | Input validation classes |

## Browser Compatibility

- Chrome 55+
- Firefox 52+
- Safari 10.1+
- Edge 15+
- Node.js 14+

For older browsers, transpile with Babel.

## Development

### Code Style
The code follows modern JavaScript best practices:
- ESLint for linting
- Consistent formatting
- Clear naming conventions
- Comprehensive comments

### Run Linter
```bash
npm install
npx eslint bankingClient.js
```

## Future Enhancements

- [ ] Add unit tests with Jest
- [ ] Implement request/response interceptors
- [ ] Add caching for account validation
- [ ] Implement transaction history retrieval
- [ ] Add rate limiting
- [ ] Create TypeScript version
- [ ] Add WebSocket support for real-time updates
- [ ] Implement metrics and monitoring

## CommonJS vs ES Modules

This implementation uses ES Modules by default. For CommonJS:

1. Remove `"type": "module"` from package.json
2. Change imports/exports to require/module.exports
3. Use node-fetch v2 instead of v3

## License
MIT License

## Author
Created for Julius Baer Banking Challenge
