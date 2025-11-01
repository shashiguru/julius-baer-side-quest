/**
 * Unit tests for Modern Banking Client (JavaScript)
 * Tests all functionality with mocked fetch responses
 */

// Mock fetch globally
global.fetch = jest.fn();

const { BankingClient, BankingClientConfig, TransferRequest, Logger } = require('./bankingClient');

describe('TransferRequest', () => {
  describe('Valid requests', () => {
    test('should create valid transfer request', () => {
      const request = new TransferRequest('ACC1000', 'ACC1001', 100.00);
      expect(request.fromAccount).toBe('ACC1000');
      expect(request.toAccount).toBe('ACC1001');
      expect(request.amount).toBe(100.00);
    });

    test('should convert to JSON correctly', () => {
      const request = new TransferRequest('ACC1000', 'ACC1001', 100.00);
      const json = request.toJSON();
      expect(json).toEqual({
        fromAccount: 'ACC1000',
        toAccount: 'ACC1001',
        amount: 100.00
      });
    });
  });

  describe('Invalid requests', () => {
    test('should throw error for empty fromAccount', () => {
      expect(() => new TransferRequest('', 'ACC1001', 100))
        .toThrow('Invalid fromAccount');
    });

    test('should throw error for empty toAccount', () => {
      expect(() => new TransferRequest('ACC1000', '', 100))
        .toThrow('Invalid toAccount');
    });

    test('should throw error for zero amount', () => {
      expect(() => new TransferRequest('ACC1000', 'ACC1001', 0))
        .toThrow('Invalid amount');
    });

    test('should throw error for negative amount', () => {
      expect(() => new TransferRequest('ACC1000', 'ACC1001', -100))
        .toThrow('Invalid amount');
    });

    test('should throw error for non-string fromAccount', () => {
      expect(() => new TransferRequest(null, 'ACC1001', 100))
        .toThrow('Invalid fromAccount');
    });
  });
});

describe('BankingClientConfig', () => {
  test('should have default values', () => {
    const config = new BankingClientConfig();
    expect(config.baseUrl).toBe('http://localhost:8123');
    expect(config.timeout).toBe(30000);
    expect(config.maxRetries).toBe(3);
  });

  test('should accept custom values', () => {
    const config = new BankingClientConfig({
      baseUrl: 'http://example.com:9000',
      timeout: 60000,
      maxRetries: 5,
      retryDelay: 2000
    });
    expect(config.baseUrl).toBe('http://example.com:9000');
    expect(config.timeout).toBe(60000);
    expect(config.maxRetries).toBe(5);
    expect(config.retryDelay).toBe(2000);
  });

  test('should remove trailing slash from baseUrl', () => {
    const config = new BankingClientConfig({ baseUrl: 'http://example.com/' });
    expect(config.baseUrl).toBe('http://example.com');
  });
});

describe('Logger', () => {
  let consoleLogSpy;

  beforeEach(() => {
    consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
  });

  afterEach(() => {
    consoleLogSpy.mockRestore();
  });

  test('should log info messages', () => {
    Logger.info('Test message');
    expect(consoleLogSpy).toHaveBeenCalled();
  });

  test('should log error messages', () => {
    Logger.error('Error message');
    expect(consoleLogSpy).toHaveBeenCalled();
  });

  test('should log with data', () => {
    Logger.info('Test', { key: 'value' });
    expect(consoleLogSpy).toHaveBeenCalledTimes(2); // Once for message, once for data
  });
});

describe('BankingClient', () => {
  let client;
  
  beforeEach(() => {
    jest.clearAllMocks();
    client = new BankingClient();
  });

  describe('Constructor', () => {
    test('should initialize with default config', () => {
      expect(client.config).toBeDefined();
      expect(client.jwtToken).toBeNull();
    });

    test('should initialize with custom config', () => {
      const config = new BankingClientConfig({ baseUrl: 'http://custom.com' });
      const customClient = new BankingClient(config);
      expect(customClient.config.baseUrl).toBe('http://custom.com');
    });
  });

  describe('authenticate', () => {
    test('should authenticate successfully', async () => {
      const mockResponse = {
        ok: true,
        status: 200,
        json: async () => ({ token: 'test_token_123' })
      };
      fetch.mockResolvedValueOnce(mockResponse);

      const result = await client.authenticate('testuser', 'password');

      expect(result).toBe(true);
      expect(client.jwtToken).toBe('test_token_123');
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/authToken'),
        expect.objectContaining({ method: 'POST' })
      );
    });

    test('should handle authentication failure', async () => {
      const mockResponse = {
        ok: false,
        status: 401,
        statusText: 'Unauthorized'
      };
      fetch.mockResolvedValueOnce(mockResponse);

      const result = await client.authenticate('testuser', 'wrongpassword');

      expect(result).toBe(false);
      expect(client.jwtToken).toBeNull();
    });

    test('should handle network error', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      const result = await client.authenticate('testuser', 'password');

      expect(result).toBe(false);
    });

    test('should handle missing token in response', async () => {
      const mockResponse = {
        ok: true,
        status: 200,
        json: async () => ({})
      };
      fetch.mockResolvedValueOnce(mockResponse);

      const result = await client.authenticate('testuser', 'password');

      expect(result).toBe(false);
    });
  });

  describe('validateAccount', () => {
    test('should validate valid account', async () => {
      const mockResponse = {
        ok: true,
        status: 200,
        json: async () => ({ valid: true })
      };
      fetch.mockResolvedValueOnce(mockResponse);

      const result = await client.validateAccount('ACC1000');

      expect(result).toBe(true);
    });

    test('should invalidate invalid account', async () => {
      const mockResponse = {
        ok: true,
        status: 200,
        json: async () => ({ valid: false })
      };
      fetch.mockResolvedValueOnce(mockResponse);

      const result = await client.validateAccount('ACC9999');

      expect(result).toBe(false);
    });

    test('should handle validation error', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      const result = await client.validateAccount('ACC1000');

      expect(result).toBe(false);
    });
  });

  describe('transferFunds', () => {
    test('should transfer funds successfully', async () => {
      const mockResponse = {
        ok: true,
        status: 200,
        json: async () => ({
          transactionId: 'txn_123',
          status: 'SUCCESS',
          message: 'Transfer completed',
          fromAccount: 'ACC1000',
          toAccount: 'ACC1001',
          amount: 100.00
        })
      };
      fetch.mockResolvedValueOnce(mockResponse);

      const result = await client.transferFunds('ACC1000', 'ACC1001', 100.00);

      expect(result).not.toBeNull();
      expect(result.transactionId).toBe('txn_123');
      expect(result.status).toBe('SUCCESS');
      expect(result.amount).toBe(100.00);
    });

    test('should transfer with authentication', async () => {
      client.jwtToken = 'test_token';
      const mockResponse = {
        ok: true,
        status: 200,
        json: async () => ({
          transactionId: 'txn_456',
          status: 'SUCCESS',
          message: 'Transfer completed',
          fromAccount: 'ACC1000',
          toAccount: 'ACC1001',
          amount: 100.00
        })
      };
      fetch.mockResolvedValueOnce(mockResponse);

      const result = await client.transferFunds('ACC1000', 'ACC1001', 100.00, true);

      expect(result).not.toBeNull();
      expect(fetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer test_token'
          })
        })
      );
    });

    test('should handle invalid transfer data', async () => {
      const result = await client.transferFunds('', 'ACC1001', 100.00);

      expect(result).toBeNull();
      expect(fetch).not.toHaveBeenCalled();
    });

    test('should handle transfer error', async () => {
      const mockResponse = {
        ok: false,
        status: 400,
        statusText: 'Bad Request',
        json: async () => ({ message: 'Invalid account' })
      };
      fetch.mockResolvedValueOnce(mockResponse);

      const result = await client.transferFunds('ACC9999', 'ACC1001', 100.00);

      expect(result).toBeNull();
    });

    test('should handle network error during transfer', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      const result = await client.transferFunds('ACC1000', 'ACC1001', 100.00);

      expect(result).toBeNull();
    });
  });

  describe('getAccounts', () => {
    test('should get accounts successfully', async () => {
      const mockAccounts = [
        { accountId: 'ACC1000', accountHolder: 'John Doe' },
        { accountId: 'ACC1001', accountHolder: 'Jane Smith' }
      ];
      const mockResponse = {
        ok: true,
        status: 200,
        json: async () => mockAccounts
      };
      fetch.mockResolvedValueOnce(mockResponse);

      const result = await client.getAccounts();

      expect(result).not.toBeNull();
      expect(result.length).toBe(2);
      expect(result[0].accountId).toBe('ACC1000');
    });

    test('should get accounts with authentication', async () => {
      client.jwtToken = 'test_token';
      const mockResponse = {
        ok: true,
        status: 200,
        json: async () => []
      };
      fetch.mockResolvedValueOnce(mockResponse);

      const result = await client.getAccounts(true);

      expect(result).not.toBeNull();
      expect(fetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer test_token'
          })
        })
      );
    });

    test('should handle error getting accounts', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      const result = await client.getAccounts();

      expect(result).toBeNull();
    });
  });

  describe('getAccountBalance', () => {
    test('should get balance successfully', async () => {
      const mockResponse = {
        ok: true,
        status: 200,
        json: async () => ({
          accountId: 'ACC1000',
          balance: 5000.00,
          currency: 'USD'
        })
      };
      fetch.mockResolvedValueOnce(mockResponse);

      const result = await client.getAccountBalance('ACC1000');

      expect(result).not.toBeNull();
      expect(result.accountId).toBe('ACC1000');
      expect(result.balance).toBe(5000.00);
    });

    test('should handle error getting balance', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      const result = await client.getAccountBalance('ACC1000');

      expect(result).toBeNull();
    });
  });

  describe('_getHeaders', () => {
    test('should return headers without auth', () => {
      const headers = client._getHeaders(false);

      expect(headers['Content-Type']).toBe('application/json');
      expect(headers['Authorization']).toBeUndefined();
    });

    test('should return headers with auth', () => {
      client.jwtToken = 'test_token_123';
      const headers = client._getHeaders(true);

      expect(headers['Content-Type']).toBe('application/json');
      expect(headers['Authorization']).toBe('Bearer test_token_123');
    });

    test('should not include auth if no token available', () => {
      const headers = client._getHeaders(true);

      expect(headers['Authorization']).toBeUndefined();
    });
  });

  describe('_fetchWithTimeout', () => {
    test('should call fetch with correct parameters', async () => {
      const mockResponse = { ok: true, status: 200 };
      fetch.mockResolvedValueOnce(mockResponse);

      const result = await client._fetchWithTimeout('http://example.com', { method: 'GET' });

      expect(result).toEqual(mockResponse);
      expect(fetch).toHaveBeenCalledWith(
        'http://example.com',
        expect.objectContaining({ method: 'GET' })
      );
    });
  });

  describe('_fetchWithRetry', () => {
    test('should succeed on first try', async () => {
      fetch.mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({}) });

      const result = await client._fetchWithRetry('http://example.com', {}, 3);

      expect(fetch).toHaveBeenCalledTimes(1);
      expect(result.ok).toBe(true);
    });

    test('should return error after network failures', async () => {
      fetch.mockRejectedValue(new Error('Network error'));

      await expect(
        client._fetchWithRetry('http://example.com', {}, 0)
      ).rejects.toThrow('Network error');

      expect(fetch).toHaveBeenCalledTimes(1);
    });
  });
});
