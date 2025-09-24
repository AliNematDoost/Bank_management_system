package com.example.mohaimen.Service;

import com.example.mohaimen.model.TransactionFeeConfig;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionFeeCalculator {

    private final TransactionFeeConfig transactionFeeConfig;
    public TransactionFeeCalculator(TransactionFeeConfig transactionFeeConfig) {
        this.transactionFeeConfig = transactionFeeConfig;
    }

    public BigDecimal CalculateFee(BigDecimal amount) {
        BigDecimal percentage = transactionFeeConfig.getPercentage();
        BigDecimal minimum = transactionFeeConfig.getMinimum();
        BigDecimal maximum = transactionFeeConfig.getMaximum();

        if (amount.multiply(percentage).compareTo(minimum) < 0) {
            return minimum;
        } else if (amount.multiply(percentage).compareTo(maximum) > 0) {
            return maximum;
        } else {
            return amount.multiply(percentage);
        }
    }

    public String getBankAccountNumber() {
        return transactionFeeConfig.getBankAccountNumber();
    }
}
