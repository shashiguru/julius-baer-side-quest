package com.banking.model;

/**
 * Transfer response model
 * Immutable object representing the API response
 */
public class TransferResponse {
    
    private final String transactionId;
    private final String status;
    private final String message;
    private final String fromAccount;
    private final String toAccount;
    private final double amount;
    
    // Constructor for Gson deserialization
    public TransferResponse(
            String transactionId,
            String status,
            String message,
            String fromAccount,
            String toAccount,
            double amount) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
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
    
    @Override
    public String toString() {
        return "TransferResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", fromAccount='" + fromAccount + '\'' +
                ", toAccount='" + toAccount + '\'' +
                ", amount=" + amount +
                '}';
    }
}
