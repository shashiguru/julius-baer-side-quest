# 🧪 Testing Quick Start Guide

## Run All Tests with One Command

### Prerequisites Check

**Python Tests:**
```bash
python --version  # Should be 3.8+
pip --version
```

**JavaScript Tests:**
```bash
node --version  # Should be 14.0+
npm --version
```

**Java Tests:**
```bash
java -version  # Should be 11+
mvn --version  # Maven 3.6+
```

---

## 🐍 Python Tests

### Option 1: Quick Run
```bash
cd submissions/modernized-solutions/python
pip install -r requirements.txt
pytest test_banking_client.py -v
```

### Option 2: With Coverage
```bash
cd submissions/modernized-solutions/python
pip install -r requirements.txt
pytest test_banking_client.py -v --cov=banking_client --cov-report=term --cov-report=html
```

### View Coverage Report
```bash
# Open in browser
start htmlcov/index.html  # Windows
open htmlcov/index.html   # Mac
xdg-open htmlcov/index.html  # Linux
```

### Expected Output
```
==================== test session starts ====================
platform win32 -- Python 3.10.0, pytest-7.4.3
collected 43 items

test_banking_client.py::TestTransferRequest::test_valid_transfer_request PASSED [ 2%]
test_banking_client.py::TestTransferRequest::test_invalid_amount_zero PASSED [ 4%]
...
test_banking_client.py::TestBankingClient::test_get_headers_with_token PASSED [100%]

---------- coverage: platform win32, python 3.10 -----------
Name                   Stmts   Miss  Cover
------------------------------------------
banking_client.py        380     35    91%
------------------------------------------
TOTAL                    380     35    91%

==================== 43 passed in 0.54s ====================
```

---

## 📜 JavaScript Tests

### Option 1: Quick Run
```bash
cd submissions/modernized-solutions/javascript
npm install
npm test
```

### Option 2: With Verbose Output
```bash
cd submissions/modernized-solutions/javascript
npm install
npm run test:verbose
```

### Option 3: Watch Mode (for development)
```bash
cd submissions/modernized-solutions/javascript
npm run test:watch
```

### View Coverage Report
```bash
# Open in browser
start coverage/lcov-report/index.html  # Windows
open coverage/lcov-report/index.html   # Mac
xdg-open coverage/lcov-report/index.html  # Linux
```

### Expected Output
```
PASS  bankingClient.test.js
  TransferRequest
    Valid requests
      ✓ should create valid transfer request (3 ms)
      ✓ should convert to JSON correctly (1 ms)
    Invalid requests
      ✓ should throw error for empty fromAccount (2 ms)
      ...
  BankingClient
    Constructor
      ✓ should initialize with default config (2 ms)
      ...

Test Suites: 1 passed, 1 total
Tests:       55 passed, 55 total
Snapshots:   0 total
Time:        2.456 s

---------------------|---------|----------|---------|---------|-------------------
File                 | % Stmts | % Branch | % Funcs | % Lines | Uncovered Line #s 
---------------------|---------|----------|---------|---------|-------------------
All files            |   88.24 |    84.62 |   91.67 |   88.89 |                   
 bankingClient.js    |   88.24 |    84.62 |   91.67 |   88.89 | 95-98,145-147     
---------------------|---------|----------|---------|---------|-------------------
```

---

## ☕ Java Tests

### Option 1: Quick Run
```bash
cd submissions/modernized-solutions/java
mvn clean test
```

### Option 2: With Coverage Report
```bash
cd submissions/modernized-solutions/java
mvn clean test jacoco:report
```

### Option 3: Full Build with Tests
```bash
cd submissions/modernized-solutions/java
mvn clean package
```

### View Coverage Report
```bash
# Open in browser
start target/site/jacoco/index.html  # Windows
open target/site/jacoco/index.html   # Mac
xdg-open target/site/jacoco/index.html  # Linux
```

### Expected Output
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.banking.BankingClientTest
[INFO] Tests run: 50, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 50, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] --- jacoco-maven-plugin:0.8.11:report ---
[INFO] Loading execution data file target/jacoco.exec
[INFO] Analyzed bundle 'banking-client-modern' with 5 classes
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  6.789 s
```

---

## 🚀 Run All Tests (All Languages)

### Windows (PowerShell)
```powershell
# Create a script: run-all-tests.ps1

# Python Tests
Write-Host "`n=== Running Python Tests ===" -ForegroundColor Cyan
cd submissions/modernized-solutions/python
pip install -r requirements.txt
pytest test_banking_client.py -v --cov=banking_client --cov-report=term

# JavaScript Tests
Write-Host "`n=== Running JavaScript Tests ===" -ForegroundColor Cyan
cd ../javascript
npm install
npm test

# Java Tests
Write-Host "`n=== Running Java Tests ===" -ForegroundColor Cyan
cd ../java
mvn clean test jacoco:report

Write-Host "`n=== All Tests Complete ===" -ForegroundColor Green
```

### Linux/Mac (Bash)
```bash
#!/bin/bash
# Create a script: run-all-tests.sh

echo -e "\n\033[1;36m=== Running Python Tests ===\033[0m"
cd submissions/modernized-solutions/python
pip install -r requirements.txt
pytest test_banking_client.py -v --cov=banking_client --cov-report=term

echo -e "\n\033[1;36m=== Running JavaScript Tests ===\033[0m"
cd ../javascript
npm install
npm test

echo -e "\n\033[1;36m=== Running Java Tests ===\033[0m"
cd ../java
mvn clean test jacoco:report

echo -e "\n\033[1;32m=== All Tests Complete ===\033[0m"
```

---

## 📊 Viewing Coverage Reports

After running tests with coverage, open these files in your browser:

| Language | Report Location |
|----------|-----------------|
| **Python** | `python/htmlcov/index.html` |
| **JavaScript** | `javascript/coverage/lcov-report/index.html` |
| **Java** | `java/target/site/jacoco/index.html` |

---

## 🐛 Troubleshooting

### Python Issues

**Problem**: `pytest: command not found`  
**Solution**: 
```bash
pip install pytest pytest-cov
```

**Problem**: `ModuleNotFoundError: No module named 'requests'`  
**Solution**:
```bash
pip install -r requirements.txt
```

### JavaScript Issues

**Problem**: `npm: command not found`  
**Solution**: Install Node.js from https://nodejs.org/

**Problem**: `Cannot find module 'jest'`  
**Solution**:
```bash
npm install
```

**Problem**: `Type: "module" not supported`  
**Solution**: The package.json has been configured for CommonJS

### Java Issues

**Problem**: `mvn: command not found`  
**Solution**: Install Maven from https://maven.apache.org/

**Problem**: `Java version mismatch`  
**Solution**: Ensure Java 11+ is installed:
```bash
java -version
```

**Problem**: `Cannot resolve dependencies`  
**Solution**:
```bash
mvn clean install -U
```

---

## 🎯 Quick Test Summary

Run this to get a summary of all test results:

```bash
# Count test files
find . -name "*test*.py" -o -name "*test*.js" -o -name "*Test*.java"

# Python test count
grep -c "def test_" python/test_banking_client.py

# JavaScript test count  
grep -c "test(" javascript/bankingClient.test.js

# Java test count
grep -c "@Test" java/src/test/java/com/banking/BankingClientTest.java
```

Expected:
- Python: 43 tests
- JavaScript: 55 tests
- Java: 50+ tests
- **Total: 148+ tests**

---

## ✅ Success Criteria

Your tests are working correctly if you see:

✅ **Python**: `43 passed` in green  
✅ **JavaScript**: `55 passed` in green  
✅ **Java**: `Tests run: 50, Failures: 0, Errors: 0`  

✅ **Coverage**: 85%+ for all languages  
✅ **No Errors**: All tests pass  
✅ **Reports Generated**: HTML coverage reports created  

---

## 📚 Next Steps

After running tests successfully:

1. ✅ Review coverage reports
2. ✅ Check uncovered lines
3. ✅ Add tests for edge cases (optional)
4. ✅ Integrate with CI/CD
5. ✅ Run before each commit

---

## 🆘 Need Help?

1. Check the full [TEST_COVERAGE_REPORT.md](TEST_COVERAGE_REPORT.md) for details
2. Review individual test files for examples
3. Check framework documentation:
   - Python: https://docs.pytest.org/
   - JavaScript: https://jestjs.io/docs/getting-started
   - Java: https://junit.org/junit5/docs/current/user-guide/

---

*Happy Testing! 🧪*
