package com.banking.config;

/**
 * Configuration class for Banking Client
 * Uses Builder pattern for flexible configuration
 */
public class BankingClientConfig {
    
    private final String baseUrl;
    private final int timeout;
    private final int maxRetries;
    private final long retryDelay;
    
    private BankingClientConfig(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.timeout = builder.timeout;
        this.maxRetries = builder.maxRetries;
        this.retryDelay = builder.retryDelay;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public long getRetryDelay() {
        return retryDelay;
    }
    
    /**
     * Builder for BankingClientConfig
     */
    public static class Builder {
        private String baseUrl = "http://localhost:8123";
        private int timeout = 30;
        private int maxRetries = 3;
        private long retryDelay = 1000; // milliseconds
        
        public Builder baseUrl(String baseUrl) {
            if (baseUrl.endsWith("/")) {
                this.baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            } else {
                this.baseUrl = baseUrl;
            }
            return this;
        }
        
        public Builder timeout(int timeout) {
            if (timeout <= 0) {
                throw new IllegalArgumentException("Timeout must be positive");
            }
            this.timeout = timeout;
            return this;
        }
        
        public Builder maxRetries(int maxRetries) {
            if (maxRetries < 0) {
                throw new IllegalArgumentException("Max retries cannot be negative");
            }
            this.maxRetries = maxRetries;
            return this;
        }
        
        public Builder retryDelay(long retryDelay) {
            if (retryDelay <= 0) {
                throw new IllegalArgumentException("Retry delay must be positive");
            }
            this.retryDelay = retryDelay;
            return this;
        }
        
        public BankingClientConfig build() {
            return new BankingClientConfig(this);
        }
    }
}
