package com.example.mohaimen.Service;

import com.example.mohaimen.model.Transaction;
import com.example.mohaimen.model.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class TransactionQuery {

    public static Specification<Transaction> filter(
            String accountNumber,
            String sourceAccountNumber,
            String destinationAccountNumber,
            TransactionType transactionType,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Date fromDate,
            Date toDate
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by account number
            if (accountNumber != null && !accountNumber.trim().isEmpty()) {
                Predicate accountPredicate = criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("sourceAccount"), accountNumber),
                        criteriaBuilder.equal(root.get("destinationAccount"), accountNumber)
                );
                predicates.add(accountPredicate);
            }

            // Filter source account number
            if (sourceAccountNumber != null && !sourceAccountNumber.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("sourceAccount"), sourceAccountNumber));
            }

            // Filter destination account number
            if (destinationAccountNumber != null && !destinationAccountNumber.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("destinationAccount"), destinationAccountNumber));
            }

            // Filter by transaction type
            if (transactionType != null) {
                predicates.add(criteriaBuilder.equal(root.get("transactionType"), transactionType));
            }

            // Filter by amount range
            if (minAmount != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            if (maxAmount != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }

            // Filter by date range
            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), fromDate));
            }
            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), toDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]) );
        };
    }
}
