package com.example.mohaimen.model;

import java.math.BigDecimal;

public class Transfer {
    private TransactionType transactionType = TransactionType.TRANSFER;
//    private String accountNumber;
//    private BigDecimal amount;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;

    public Transfer() {
    }

    public Transfer(String fromAccount, String toAccount, BigDecimal amount) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public  TransactionType getTransactionType() {
        return transactionType;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
