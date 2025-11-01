"""
Unit tests for Modern Banking Client
Tests all functionality with mocked HTTP responses
"""

import pytest
from unittest.mock import Mock, patch, MagicMock
import requests
from banking_client import (
    BankingClient,
    BankingClientConfig,
    TransferRequest,
    TransferResponse
)


class TestTransferRequest:
    """Test TransferRequest data class validation"""
    
    def test_valid_transfer_request(self):
        """Test creating a valid transfer request"""
        request = TransferRequest(
            from_account="ACC1000",
            to_account="ACC1001",
            amount=100.0
        )
        assert request.from_account == "ACC1000"
        assert request.to_account == "ACC1001"
        assert request.amount == 100.0
    
    def test_invalid_amount_zero(self):
        """Test that zero amount raises ValueError"""
        with pytest.raises(ValueError, match="Amount must be greater than 0"):
            TransferRequest("ACC1000", "ACC1001", 0)
    
    def test_invalid_amount_negative(self):
        """Test that negative amount raises ValueError"""
        with pytest.raises(ValueError, match="Amount must be greater than 0"):
            TransferRequest("ACC1000", "ACC1001", -100)
    
    def test_empty_from_account(self):
        """Test that empty from_account raises ValueError"""
        with pytest.raises(ValueError, match="Account numbers cannot be empty"):
            TransferRequest("", "ACC1001", 100)
    
    def test_empty_to_account(self):
        """Test that empty to_account raises ValueError"""
        with pytest.raises(ValueError, match="Account numbers cannot be empty"):
            TransferRequest("ACC1000", "", 100)


class TestBankingClientConfig:
    """Test BankingClientConfig class"""
    
    def test_default_config(self):
        """Test default configuration values"""
        config = BankingClientConfig()
        assert config.base_url == "http://localhost:8123"
        assert config.timeout == 30
        assert config.max_retries == 3
    
    def test_custom_config(self):
        """Test custom configuration values"""
        config = BankingClientConfig(
            base_url="http://example.com:9000",
            timeout=60,
            max_retries=5
        )
        assert config.base_url == "http://example.com:9000"
        assert config.timeout == 60
        assert config.max_retries == 5


class TestBankingClient:
    """Test BankingClient class"""
    
    @pytest.fixture
    def client(self):
        """Create a banking client for testing"""
        config = BankingClientConfig()
        return BankingClient(config)
    
    @pytest.fixture
    def mock_response(self):
        """Create a mock response object"""
        mock = Mock()
        mock.status_code = 200
        mock.raise_for_status = Mock()
        return mock
    
    def test_client_initialization(self, client):
        """Test client initializes correctly"""
        assert client.config is not None
        assert client.session is not None
        assert client.jwt_token is None
    
    def test_close(self, client):
        """Test client close method"""
        client.close()
        # No exception should be raised
    
    # Authentication Tests
    @patch('banking_client.requests.Session.post')
    def test_authenticate_success(self, mock_post, client):
        """Test successful authentication"""
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {'token': 'test_token_123'}
        mock_post.return_value = mock_response
        
        result = client.authenticate("testuser", "password")
        
        assert result is True
        assert client.jwt_token == 'test_token_123'
        mock_post.assert_called_once()
    
    @patch('banking_client.requests.Session.post')
    def test_authenticate_no_token_in_response(self, mock_post, client):
        """Test authentication with no token in response"""
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {}
        mock_post.return_value = mock_response
        
        result = client.authenticate("testuser", "password")
        
        assert result is False
        assert client.jwt_token is None
    
    @patch('banking_client.requests.Session.post')
    def test_authenticate_http_error(self, mock_post, client):
        """Test authentication with HTTP error"""
        mock_post.side_effect = requests.exceptions.HTTPError("401 Unauthorized")
        
        result = client.authenticate("testuser", "wrongpassword")
        
        assert result is False
        assert client.jwt_token is None
    
    @patch('banking_client.requests.Session.post')
    def test_authenticate_connection_error(self, mock_post, client):
        """Test authentication with connection error"""
        mock_post.side_effect = requests.exceptions.ConnectionError("Connection refused")
        
        result = client.authenticate("testuser", "password")
        
        assert result is False
    
    # Account Validation Tests
    @patch('banking_client.requests.Session.get')
    def test_validate_account_success(self, mock_get, client):
        """Test successful account validation"""
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {'valid': True}
        mock_get.return_value = mock_response
        
        result = client.validate_account("ACC1000")
        
        assert result is True
        mock_get.assert_called_once()
    
    @patch('banking_client.requests.Session.get')
    def test_validate_account_invalid(self, mock_get, client):
        """Test validation of invalid account"""
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {'valid': False}
        mock_get.return_value = mock_response
        
        result = client.validate_account("ACC9999")
        
        assert result is False
    
    @patch('banking_client.requests.Session.get')
    def test_validate_account_http_error(self, mock_get, client):
        """Test account validation with HTTP error"""
        mock_get.side_effect = requests.exceptions.HTTPError("404 Not Found")
        
        result = client.validate_account("ACC9999")
        
        assert result is False
    
    # Transfer Tests
    @patch('banking_client.requests.Session.post')
    def test_transfer_funds_success(self, mock_post, client):
        """Test successful transfer"""
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {
            'transactionId': 'txn_123',
            'status': 'SUCCESS',
            'message': 'Transfer completed',
            'fromAccount': 'ACC1000',
            'toAccount': 'ACC1001',
            'amount': 100.0
        }
        mock_post.return_value = mock_response
        
        result = client.transfer_funds("ACC1000", "ACC1001", 100.0)
        
        assert result is not None
        assert result.transaction_id == 'txn_123'
        assert result.status == 'SUCCESS'
        assert result.amount == 100.0
    
    @patch('banking_client.requests.Session.post')
    def test_transfer_funds_with_auth(self, mock_post, client):
        """Test transfer with authentication"""
        client.jwt_token = 'test_token'
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {
            'transactionId': 'txn_456',
            'status': 'SUCCESS',
            'message': 'Transfer completed',
            'fromAccount': 'ACC1000',
            'toAccount': 'ACC1001',
            'amount': 100.0
        }
        mock_post.return_value = mock_response
        
        result = client.transfer_funds("ACC1000", "ACC1001", 100.0, use_auth=True)
        
        assert result is not None
        assert result.transaction_id == 'txn_456'
    
    @patch('banking_client.requests.Session.post')
    def test_transfer_funds_invalid_amount(self, mock_post, client):
        """Test transfer with invalid amount"""
        result = client.transfer_funds("ACC1000", "ACC1001", -50.0)
        
        assert result is None
        mock_post.assert_not_called()
    
    @patch('banking_client.requests.Session.post')
    def test_transfer_funds_http_error(self, mock_post, client):
        """Test transfer with HTTP error"""
        mock_response = Mock()
        mock_response.status_code = 400
        mock_response.raise_for_status.side_effect = requests.exceptions.HTTPError("400 Bad Request")
        mock_post.return_value = mock_response
        
        result = client.transfer_funds("ACC1000", "ACC1001", 100.0)
        
        assert result is None
    
    @patch('banking_client.requests.Session.post')
    def test_transfer_funds_connection_error(self, mock_post, client):
        """Test transfer with connection error"""
        mock_post.side_effect = requests.exceptions.ConnectionError("Connection refused")
        
        result = client.transfer_funds("ACC1000", "ACC1001", 100.0)
        
        assert result is None
    
    # Get Accounts Tests
    @patch('banking_client.requests.Session.get')
    def test_get_accounts_success(self, mock_get, client):
        """Test successful get accounts"""
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = [
            {'accountId': 'ACC1000', 'accountHolder': 'John Doe'},
            {'accountId': 'ACC1001', 'accountHolder': 'Jane Smith'}
        ]
        mock_get.return_value = mock_response
        
        result = client.get_accounts()
        
        assert result is not None
        assert len(result) == 2
        assert result[0]['accountId'] == 'ACC1000'
    
    @patch('banking_client.requests.Session.get')
    def test_get_accounts_with_auth(self, mock_get, client):
        """Test get accounts with authentication"""
        client.jwt_token = 'test_token'
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = []
        mock_get.return_value = mock_response
        
        result = client.get_accounts(use_auth=True)
        
        assert result is not None
        assert len(result) == 0
    
    @patch('banking_client.requests.Session.get')
    def test_get_accounts_error(self, mock_get, client):
        """Test get accounts with error"""
        mock_get.side_effect = requests.exceptions.RequestException("Network error")
        
        result = client.get_accounts()
        
        assert result is None
    
    # Get Account Balance Tests
    @patch('banking_client.requests.Session.get')
    def test_get_account_balance_success(self, mock_get, client):
        """Test successful get account balance"""
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {
            'accountId': 'ACC1000',
            'balance': 5000.00,
            'currency': 'USD'
        }
        mock_get.return_value = mock_response
        
        result = client.get_account_balance("ACC1000")
        
        assert result is not None
        assert result['accountId'] == 'ACC1000'
        assert result['balance'] == 5000.00
    
    @patch('banking_client.requests.Session.get')
    def test_get_account_balance_error(self, mock_get, client):
        """Test get account balance with error"""
        mock_get.side_effect = requests.exceptions.RequestException("Network error")
        
        result = client.get_account_balance("ACC1000")
        
        assert result is None
    
    # Header Tests
    def test_get_headers_without_token(self, client):
        """Test headers without JWT token"""
        headers = client._get_headers()
        
        assert 'Content-Type' in headers
        assert headers['Content-Type'] == 'application/json'
        assert 'Authorization' not in headers
    
    def test_get_headers_with_token(self, client):
        """Test headers with JWT token"""
        client.jwt_token = 'test_token_123'
        headers = client._get_headers()
        
        assert 'Content-Type' in headers
        assert 'Authorization' in headers
        assert headers['Authorization'] == 'Bearer test_token_123'


if __name__ == '__main__':
    pytest.main([__file__, '-v', '--cov=banking_client', '--cov-report=term-missing', '--cov-report=html'])
