package com.example.mohaimen.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "TRANSACTION")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long TrackingCode;

    private TransactionType transactionType;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private Status status = Status.PENDING;
    private Date date;

    public Transaction(TransactionType transactionType,
                       String sourceAccount,
                       String destinationAccount,
                       BigDecimal amount,
                       Date date) {
        this.amount = amount;
        this.transactionType = transactionType;
        this.date = date;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
    }

    public Transaction() {
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Long getTrackingCode() {
        return TrackingCode;
    }

    public void setTrackingCode(Long id) {
        this.TrackingCode = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
