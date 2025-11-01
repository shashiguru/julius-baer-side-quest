/**
 * Modern JavaScript ES6+ Banking Client
 * =====================================
 * A modernized implementation of the legacy ES5 banking client with:
 * - ES6+ syntax (const/let, arrow functions, classes)
 * - Modern fetch API instead of XMLHttpRequest
 * - Async/await pattern for asynchronous operations
 * - Comprehensive error handling
 * - Template literals for string interpolation
 * - Input validation and sanitization
 * - Structured logging
 */

// Configuration class for client settings
class BankingClientConfig {
  constructor({
    baseUrl = 'http://localhost:8123',
    timeout = 30000,
    maxRetries = 3,
    retryDelay = 1000
  } = {}) {
    this.baseUrl = baseUrl.replace(/\/$/, ''); // Remove trailing slash
    this.timeout = timeout;
    this.maxRetries = maxRetries;
    this.retryDelay = retryDelay;
  }
}

// Transfer request class with validation
class TransferRequest {
  constructor(fromAccount, toAccount, amount) {
    this.validate(fromAccount, toAccount, amount);
    this.fromAccount = fromAccount;
    this.toAccount = toAccount;
    this.amount = amount;
  }

  validate(fromAccount, toAccount, amount) {
    if (!fromAccount || typeof fromAccount !== 'string') {
      throw new Error('Invalid fromAccount: must be a non-empty string');
    }
    if (!toAccount || typeof toAccount !== 'string') {
      throw new Error('Invalid toAccount: must be a non-empty string');
    }
    if (typeof amount !== 'number' || amount <= 0) {
      throw new Error('Invalid amount: must be a positive number');
    }
  }

  toJSON() {
    return {
      fromAccount: this.fromAccount,
      toAccount: this.toAccount,
      amount: this.amount
    };
  }
}

// Logger utility for structured logging
class Logger {
  static log(level, message, data = null) {
    const timestamp = new Date().toISOString();
    const logEntry = {
      timestamp,
      level: level.toUpperCase(),
      message,
      ...(data && { data })
    };
    
    const colorCodes = {
      INFO: '\x1b[36m',    // Cyan
      SUCCESS: '\x1b[32m', // Green
      WARN: '\x1b[33m',    // Yellow
      ERROR: '\x1b[31m',   // Red
      RESET: '\x1b[0m'
    };
    
    const color = colorCodes[level.toUpperCase()] || colorCodes.RESET;
    console.log(`${color}[${timestamp}] ${level.toUpperCase()}: ${message}${colorCodes.RESET}`);
    if (data) {
      console.log(JSON.stringify(data, null, 2));
    }
  }

  static info(message, data) {
    this.log('INFO', message, data);
  }

  static success(message, data) {
    this.log('SUCCESS', message, data);
  }

  static warn(message, data) {
    this.log('WARN', message, data);
  }

  static error(message, data) {
    this.log('ERROR', message, data);
  }
}

// Modern Banking Client class
class BankingClient {
  constructor(config = new BankingClientConfig()) {
    this.config = config;
    this.jwtToken = null;
    Logger.info(`Banking client initialized with base URL: ${this.config.baseUrl}`);
  }

  /**
   * Create a fetch request with timeout support
   * @private
   */
  async _fetchWithTimeout(url, options = {}) {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.config.timeout);
    
    try {
      const response = await fetch(url, {
        ...options,
        signal: controller.signal
      });
      clearTimeout(timeoutId);
      return response;
    } catch (error) {
      clearTimeout(timeoutId);
      if (error.name === 'AbortError') {
        throw new Error(`Request timeout after ${this.config.timeout}ms`);
      }
      throw error;
    }
  }

  /**
   * Retry logic with exponential backoff
   * @private
   */
  async _fetchWithRetry(url, options = {}, retries = this.config.maxRetries) {
    try {
      return await this._fetchWithTimeout(url, options);
    } catch (error) {
      if (retries > 0) {
        const delay = this.config.retryDelay * (this.config.maxRetries - retries + 1);
        Logger.warn(`Request failed, retrying in ${delay}ms... (${retries} retries left)`);
        await new Promise(resolve => setTimeout(resolve, delay));
        return this._fetchWithRetry(url, options, retries - 1);
      }
      throw error;
    }
  }

  /**
   * Get headers with optional JWT token
   * @private
   */
  _getHeaders(includeAuth = false) {
    const headers = {
      'Content-Type': 'application/json'
    };
    
    if (includeAuth && this.jwtToken) {
      headers['Authorization'] = `Bearer ${this.jwtToken}`;
    }
    
    return headers;
  }

  /**
   * Authenticate with the banking API
   * @param {string} username - Username for authentication
   * @param {string} password - Password for authentication
   * @returns {Promise<boolean>} True if authentication successful
   */
  async authenticate(username = 'testuser', password = 'password') {
    const url = `${this.config.baseUrl}/authToken`;
    
    try {
      Logger.info(`Attempting authentication for user: ${username}`);
      
      const response = await this._fetchWithRetry(url, {
        method: 'POST',
        headers: this._getHeaders(),
        body: JSON.stringify({ username, password })
      });

      if (!response.ok) {
        throw new Error(`Authentication failed: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      this.jwtToken = data.token;

      if (this.jwtToken) {
        Logger.success('Authentication successful');
        return true;
      } else {
        Logger.error('No token received in response');
        return false;
      }
    } catch (error) {
      Logger.error('Authentication failed', { error: error.message });
      return false;
    }
  }

  /**
   * Validate if an account exists and is active
   * @param {string} accountId - Account ID to validate
   * @returns {Promise<boolean>} True if account is valid
   */
  async validateAccount(accountId) {
    const url = `${this.config.baseUrl}/accounts/validate/${accountId}`;
    
    try {
      Logger.info(`Validating account: ${accountId}`);
      
      const response = await this._fetchWithRetry(url, {
        method: 'GET',
        headers: this._getHeaders()
      });

      if (!response.ok) {
        throw new Error(`Validation failed: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      const isValid = data.valid || false;
      
      Logger.info(`Account ${accountId} validation result: ${isValid}`);
      return isValid;
    } catch (error) {
      Logger.error('Account validation failed', { accountId, error: error.message });
      return false;
    }
  }

  /**
   * Transfer funds between accounts
   * @param {string} fromAccount - Source account ID
   * @param {string} toAccount - Destination account ID
   * @param {number} amount - Amount to transfer
   * @param {boolean} useAuth - Whether to use JWT authentication
   * @returns {Promise<Object|null>} Transfer response object or null
   */
  async transferFunds(fromAccount, toAccount, amount, useAuth = false) {
    const url = `${this.config.baseUrl}/transfer`;
    
    try {
      // Validate request
      const request = new TransferRequest(fromAccount, toAccount, amount);
      Logger.info(
        `Initiating transfer: ${request.fromAccount} -> ${request.toAccount}, Amount: $${request.amount}`
      );

      // Make request
      const response = await this._fetchWithRetry(url, {
        method: 'POST',
        headers: this._getHeaders(useAuth),
        body: JSON.stringify(request.toJSON())
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(
          `Transfer failed: ${response.status} ${response.statusText}` +
          (errorData.message ? ` - ${errorData.message}` : '')
        );
      }

      const data = await response.json();
      
      Logger.success(
        `Transfer successful! Transaction ID: ${data.transactionId}, Status: ${data.status}`
      );
      
      return {
        transactionId: data.transactionId,
        status: data.status,
        message: data.message,
        fromAccount: data.fromAccount,
        toAccount: data.toAccount,
        amount: data.amount
      };
    } catch (error) {
      Logger.error('Transfer failed', { error: error.message });
      return null;
    }
  }

  /**
   * Retrieve list of all accounts
   * @param {boolean} useAuth - Whether to use JWT authentication
   * @returns {Promise<Array|null>} Array of accounts or null
   */
  async getAccounts(useAuth = false) {
    const url = `${this.config.baseUrl}/accounts`;
    
    try {
      Logger.info('Fetching accounts list');
      
      const response = await this._fetchWithRetry(url, {
        method: 'GET',
        headers: this._getHeaders(useAuth)
      });

      if (!response.ok) {
        throw new Error(`Failed to get accounts: ${response.status} ${response.statusText}`);
      }

      const accounts = await response.json();
      Logger.success(`Retrieved ${accounts.length} accounts`);
      return accounts;
    } catch (error) {
      Logger.error('Failed to retrieve accounts', { error: error.message });
      return null;
    }
  }

  /**
   * Get account balance
   * @param {string} accountId - Account ID
   * @param {boolean} useAuth - Whether to use JWT authentication
   * @returns {Promise<Object|null>} Balance information or null
   */
  async getAccountBalance(accountId, useAuth = false) {
    const url = `${this.config.baseUrl}/accounts/balance/${accountId}`;
    
    try {
      Logger.info(`Fetching balance for account: ${accountId}`);
      
      const response = await this._fetchWithRetry(url, {
        method: 'GET',
        headers: this._getHeaders(useAuth)
      });

      if (!response.ok) {
        throw new Error(`Failed to get balance: ${response.status} ${response.statusText}`);
      }

      const balanceData = await response.json();
      Logger.success(`Balance retrieved for ${accountId}: $${balanceData.balance}`);
      return balanceData;
    } catch (error) {
      Logger.error('Failed to retrieve balance', { accountId, error: error.message });
      return null;
    }
  }
}

// Demo function
async function main() {
  console.log('='.repeat(60));
  console.log('Modern Banking Client - JavaScript ES6+ Implementation');
  console.log('='.repeat(60));

  // Initialize client
  const config = new BankingClientConfig({ baseUrl: 'http://localhost:8123' });
  const client = new BankingClient(config);

  try {
    // Example 1: Basic transfer without authentication
    console.log('\n[1] Basic Transfer (No Authentication)');
    console.log('-'.repeat(60));
    const result1 = await client.transferFunds('ACC1000', 'ACC1001', 100.00);
    if (result1) {
      console.log(`✓ Transaction ID: ${result1.transactionId}`);
      console.log(`✓ Status: ${result1.status}`);
      console.log(`✓ Message: ${result1.message}`);
    } else {
      console.log('✗ Transfer failed');
    }

    // Example 2: Transfer with authentication
    console.log('\n[2] Transfer with JWT Authentication');
    console.log('-'.repeat(60));
    const authenticated = await client.authenticate('testuser', 'password');
    if (authenticated) {
      console.log('✓ Authentication successful');
      const result2 = await client.transferFunds('ACC1002', 'ACC1003', 250.50, true);
      if (result2) {
        console.log(`✓ Transaction ID: ${result2.transactionId}`);
        console.log(`✓ Status: ${result2.status}`);
      }
    } else {
      console.log('✗ Authentication failed');
    }

    // Example 3: Account validation
    console.log('\n[3] Account Validation');
    console.log('-'.repeat(60));
    const accountsToValidate = ['ACC1000', 'ACC2000', 'ACC9999'];
    for (const acc of accountsToValidate) {
      const isValid = await client.validateAccount(acc);
      const status = isValid ? '✓ Valid' : '✗ Invalid';
      console.log(`${status}: ${acc}`);
    }

    // Example 4: Get all accounts
    console.log('\n[4] Retrieve All Accounts');
    console.log('-'.repeat(60));
    const accounts = await client.getAccounts();
    if (accounts) {
      console.log(`✓ Found ${accounts.length} accounts:`);
      accounts.slice(0, 5).forEach(acc => {
        console.log(`  - ${acc.accountId || 'N/A'}: ${acc.accountHolder || 'N/A'}`);
      });
    } else {
      console.log('✗ Failed to retrieve accounts');
    }

    // Example 5: Error handling - Invalid transfer
    console.log('\n[5] Error Handling Demo (Invalid Account)');
    console.log('-'.repeat(60));
    const result5 = await client.transferFunds('ACC9999', 'ACC1001', 50.00);
    if (!result5) {
      console.log('✓ Error handled gracefully - check logs for details');
    }

  } catch (error) {
    Logger.error('Demo failed', { error: error.message });
  }

  console.log('\n' + '='.repeat(60));
  console.log('Demo completed!');
  console.log('='.repeat(60));
}

// Export for use as module
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { BankingClient, BankingClientConfig, TransferRequest, Logger };
}

// Run demo if executed directly
if (typeof require !== 'undefined' && require.main === module) {
  main().catch(error => {
    Logger.error('Fatal error', { error: error.message });
    process.exit(1);
  });
}
