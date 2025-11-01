"""
Modern Python 3.x Banking Client
---------------------------------
A modernized implementation of the legacy Python 2.7 banking client with:
- Python 3.x syntax and features
- Modern requests library
- JWT authentication support
- Comprehensive error handling
- Structured logging
- Type hints
- Configuration management
"""

import logging
import sys
from typing import Dict, Optional, Any
from dataclasses import dataclass
import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

# Configure structured logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler('banking_client.log')
    ]
)
logger = logging.getLogger(__name__)


@dataclass
class TransferRequest:
    """Data class for transfer requests with validation"""
    from_account: str
    to_account: str
    amount: float

    def __post_init__(self):
        """Validate transfer request data"""
        if self.amount <= 0:
            raise ValueError("Amount must be greater than 0")
        if not self.from_account or not self.to_account:
            raise ValueError("Account numbers cannot be empty")


@dataclass
class TransferResponse:
    """Data class for transfer responses"""
    transaction_id: str
    status: str
    message: str
    from_account: str
    to_account: str
    amount: float


class BankingClientConfig:
    """Configuration management for banking client"""
    
    def __init__(
        self, 
        base_url: str = "http://localhost:8123",
        timeout: int = 30,
        max_retries: int = 3
    ):
        self.base_url = base_url.rstrip('/')
        self.timeout = timeout
        self.max_retries = max_retries


class BankingClient:
    """Modern banking client with REST API integration"""
    
    def __init__(self, config: Optional[BankingClientConfig] = None):
        """
        Initialize the banking client
        
        Args:
            config: Optional configuration object
        """
        self.config = config or BankingClientConfig()
        self.session = self._create_session()
        self.jwt_token: Optional[str] = None
        logger.info(f"Banking client initialized with base URL: {self.config.base_url}")
    
    def _create_session(self) -> requests.Session:
        """
        Create a requests session with connection pooling and retry logic
        
        Returns:
            Configured requests Session object
        """
        session = requests.Session()
        
        # Configure retry strategy with exponential backoff
        retry_strategy = Retry(
            total=self.config.max_retries,
            backoff_factor=1,
            status_forcelist=[429, 500, 502, 503, 504],
            allowed_methods=["GET", "POST"]
        )
        
        adapter = HTTPAdapter(max_retries=retry_strategy)
        session.mount("http://", adapter)
        session.mount("https://", adapter)
        
        return session
    
    def authenticate(self, username: str = "testuser", password: str = "password") -> bool:
        """
        Authenticate with the banking API and retrieve JWT token
        
        Args:
            username: Username for authentication
            password: Password for authentication
            
        Returns:
            True if authentication successful, False otherwise
        """
        url = f"{self.config.base_url}/authToken"
        payload = {
            "username": username,
            "password": password
        }
        
        try:
            logger.info(f"Attempting authentication for user: {username}")
            response = self.session.post(
                url,
                json=payload,
                timeout=self.config.timeout,
                headers={"Content-Type": "application/json"}
            )
            response.raise_for_status()
            
            data = response.json()
            self.jwt_token = data.get('token')
            
            if self.jwt_token:
                logger.info("Authentication successful")
                return True
            else:
                logger.error("No token received in response")
                return False
                
        except requests.exceptions.RequestException as e:
            logger.error(f"Authentication failed: {e}")
            return False
    
    def validate_account(self, account_id: str) -> bool:
        """
        Validate if an account exists and is active
        
        Args:
            account_id: Account ID to validate
            
        Returns:
            True if account is valid, False otherwise
        """
        url = f"{self.config.base_url}/accounts/validate/{account_id}"
        
        try:
            logger.info(f"Validating account: {account_id}")
            response = self.session.get(
                url,
                timeout=self.config.timeout,
                headers=self._get_headers()
            )
            response.raise_for_status()
            
            data = response.json()
            is_valid = data.get('valid', False)
            logger.info(f"Account {account_id} validation result: {is_valid}")
            return is_valid
            
        except requests.exceptions.RequestException as e:
            logger.error(f"Account validation failed: {e}")
            return False
    
    def transfer_funds(
        self, 
        from_account: str, 
        to_account: str, 
        amount: float,
        use_auth: bool = False
    ) -> Optional[TransferResponse]:
        """
        Transfer funds between accounts
        
        Args:
            from_account: Source account ID
            to_account: Destination account ID
            amount: Amount to transfer
            use_auth: Whether to use JWT authentication
            
        Returns:
            TransferResponse object if successful, None otherwise
        """
        url = f"{self.config.base_url}/transfer"
        
        try:
            # Validate request
            request = TransferRequest(from_account, to_account, amount)
            logger.info(
                f"Initiating transfer: {request.from_account} -> "
                f"{request.to_account}, Amount: ${request.amount}"
            )
            
            # Prepare payload
            payload = {
                "fromAccount": request.from_account,
                "toAccount": request.to_account,
                "amount": request.amount
            }
            
            # Make request
            headers = self._get_headers() if use_auth else {"Content-Type": "application/json"}
            response = self.session.post(
                url,
                json=payload,
                timeout=self.config.timeout,
                headers=headers
            )
            
            # Handle response
            response.raise_for_status()
            data = response.json()
            
            transfer_response = TransferResponse(
                transaction_id=data.get('transactionId', ''),
                status=data.get('status', ''),
                message=data.get('message', ''),
                from_account=data.get('fromAccount', ''),
                to_account=data.get('toAccount', ''),
                amount=data.get('amount', 0.0)
            )
            
            logger.info(
                f"Transfer successful! Transaction ID: {transfer_response.transaction_id}, "
                f"Status: {transfer_response.status}"
            )
            return transfer_response
            
        except ValueError as e:
            logger.error(f"Invalid transfer request: {e}")
            return None
        except requests.exceptions.HTTPError as e:
            logger.error(f"HTTP error occurred: {e.response.status_code} - {e.response.text}")
            return None
        except requests.exceptions.RequestException as e:
            logger.error(f"Transfer failed: {e}")
            return None
    
    def get_accounts(self, use_auth: bool = False) -> Optional[list]:
        """
        Retrieve list of all accounts
        
        Args:
            use_auth: Whether to use JWT authentication
            
        Returns:
            List of accounts if successful, None otherwise
        """
        url = f"{self.config.base_url}/accounts"
        
        try:
            logger.info("Fetching accounts list")
            headers = self._get_headers() if use_auth else {}
            response = self.session.get(
                url,
                timeout=self.config.timeout,
                headers=headers
            )
            response.raise_for_status()
            
            accounts = response.json()
            logger.info(f"Retrieved {len(accounts)} accounts")
            return accounts
            
        except requests.exceptions.RequestException as e:
            logger.error(f"Failed to retrieve accounts: {e}")
            return None
    
    def get_account_balance(self, account_id: str, use_auth: bool = False) -> Optional[Dict[str, Any]]:
        """
        Get account balance
        
        Args:
            account_id: Account ID
            use_auth: Whether to use JWT authentication
            
        Returns:
            Balance information if successful, None otherwise
        """
        url = f"{self.config.base_url}/accounts/balance/{account_id}"
        
        try:
            logger.info(f"Fetching balance for account: {account_id}")
            headers = self._get_headers() if use_auth else {}
            response = self.session.get(
                url,
                timeout=self.config.timeout,
                headers=headers
            )
            response.raise_for_status()
            
            balance_data = response.json()
            logger.info(f"Balance retrieved for {account_id}: ${balance_data.get('balance', 'N/A')}")
            return balance_data
            
        except requests.exceptions.RequestException as e:
            logger.error(f"Failed to retrieve balance: {e}")
            return None
    
    def _get_headers(self) -> Dict[str, str]:
        """
        Get headers including JWT token if available
        
        Returns:
            Dictionary of HTTP headers
        """
        headers = {"Content-Type": "application/json"}
        if self.jwt_token:
            headers["Authorization"] = f"Bearer {self.jwt_token}"
        return headers
    
    def close(self):
        """Close the session and cleanup resources"""
        self.session.close()
        logger.info("Banking client session closed")


def main():
    """Main demonstration function"""
    print("=" * 60)
    print("Modern Banking Client - Python 3.x Implementation")
    print("=" * 60)
    
    # Initialize client
    config = BankingClientConfig(base_url="http://localhost:8123")
    client = BankingClient(config)
    
    try:
        # Example 1: Basic transfer without authentication
        print("\n[1] Basic Transfer (No Authentication)")
        print("-" * 60)
        result = client.transfer_funds("ACC1000", "ACC1001", 100.00)
        if result:
            print(f"✓ Transaction ID: {result.transaction_id}")
            print(f"✓ Status: {result.status}")
            print(f"✓ Message: {result.message}")
        else:
            print("✗ Transfer failed")
        
        # Example 2: Transfer with authentication
        print("\n[2] Transfer with JWT Authentication")
        print("-" * 60)
        if client.authenticate("testuser", "password"):
            print("✓ Authentication successful")
            result = client.transfer_funds("ACC1002", "ACC1003", 250.50, use_auth=True)
            if result:
                print(f"✓ Transaction ID: {result.transaction_id}")
                print(f"✓ Status: {result.status}")
        else:
            print("✗ Authentication failed")
        
        # Example 3: Account validation
        print("\n[3] Account Validation")
        print("-" * 60)
        accounts_to_validate = ["ACC1000", "ACC2000", "ACC9999"]
        for acc in accounts_to_validate:
            is_valid = client.validate_account(acc)
            status = "✓ Valid" if is_valid else "✗ Invalid"
            print(f"{status}: {acc}")
        
        # Example 4: Get all accounts
        print("\n[4] Retrieve All Accounts")
        print("-" * 60)
        accounts = client.get_accounts()
        if accounts:
            print(f"✓ Found {len(accounts)} accounts:")
            for acc in accounts[:5]:  # Show first 5
                print(f"  - {acc.get('accountId', 'N/A')}: {acc.get('accountHolder', 'N/A')}")
        else:
            print("✗ Failed to retrieve accounts")
        
        # Example 5: Get account balance
        print("\n[5] Get Account Balance")
        print("-" * 60)
        balance = client.get_account_balance("ACC1000")
        if balance:
            print(f"✓ Account: {balance.get('accountId', 'N/A')}")
            print(f"✓ Balance: ${balance.get('balance', 'N/A')}")
        else:
            print("✗ Failed to retrieve balance")
        
        # Example 6: Error handling - Invalid transfer
        print("\n[6] Error Handling Demo (Invalid Account)")
        print("-" * 60)
        result = client.transfer_funds("ACC9999", "ACC1001", 50.00)
        if not result:
            print("✓ Error handled gracefully - check logs for details")
        
    finally:
        client.close()
        print("\n" + "=" * 60)
        print("Demo completed!")
        print("=" * 60)


if __name__ == "__main__":
    main()
