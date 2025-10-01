package com.example.mohaimen.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "account_change_logs")
public class AccountChangeLogs {
    @Id
    @SequenceGenerator(
            name = "audit_seq",
            sequenceName = "account_audit_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "audit_seq"
    )
    private Long id;

    private String accountNumber;

    private String changedField;

    private String oldValue;

    private String newValue;

    @Column(name = "changed_by")
    private String changedBy;

    private Date changedDate;

    public AccountChangeLogs() {
    }

    public AccountChangeLogs(String accountNumber, String changedField, String oldValue, String newValue, String changedBy, Date changedDate) {
        this.accountNumber = accountNumber;
        this.changedField = changedField;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
        this.changedDate = changedDate;
    }
}
