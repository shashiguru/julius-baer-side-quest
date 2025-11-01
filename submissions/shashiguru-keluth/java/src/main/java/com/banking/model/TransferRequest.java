package com.banking.model;

/**
 * Transfer request model with validation
 * Immutable object using Builder pattern
 */
public class TransferRequest {
    
    private final String fromAccount;
    private final String toAccount;
    private final double amount;
    
    private TransferRequest(Builder builder) {
        this.fromAccount = builder.fromAccount;
        this.toAccount = builder.toAccount;
        this.amount = builder.amount;
    }
    
    public String getFromAccount() {
        return fromAccount;
    }
    
    public String getToAccount() {
        return toAccount;
    }
    
    public double getAmount() {
        return amount;
    }
    
    /**
     * Builder for TransferRequest with validation
     */
    public static class Builder {
        private String fromAccount;
        private String toAccount;
        private double amount;
        
        public Builder fromAccount(String fromAccount) {
            if (fromAccount == null || fromAccount.trim().isEmpty()) {
                throw new IllegalArgumentException("fromAccount cannot be null or empty");
            }
            this.fromAccount = fromAccount;
            return this;
        }
        
        public Builder toAccount(String toAccount) {
            if (toAccount == null || toAccount.trim().isEmpty()) {
                throw new IllegalArgumentException("toAccount cannot be null or empty");
            }
            this.toAccount = toAccount;
            return this;
        }
        
        public Builder amount(double amount) {
            if (amount <= 0) {
                throw new IllegalArgumentException("amount must be greater than 0");
            }
            this.amount = amount;
            return this;
        }
        
        public TransferRequest build() {
            if (fromAccount == null) {
                throw new IllegalStateException("fromAccount must be set");
            }
            if (toAccount == null) {
                throw new IllegalStateException("toAccount must be set");
            }
            if (amount <= 0) {
                throw new IllegalStateException("amount must be greater than 0");
            }
            return new TransferRequest(this);
        }
    }
}
