package com.example.mohaimen.Controller;

import com.example.mohaimen.Repository.AccountRepository;
import com.example.mohaimen.Repository.TransactionRepository;
import com.example.mohaimen.Service.TransactionFeeCalculator;
import com.example.mohaimen.Service.TransactionQuery;
import com.example.mohaimen.Service.TransactionService;
import com.example.mohaimen.model.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/Transaction")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // DEPOSIT AMOUNT TO AN ACCOUNT
    @Transactional
    @PostMapping("")
    public ResponseEntity<?> depositOrWithdraw(@RequestBody DepositTran depositTran) {
        return transactionService.depositOrWithdrawal(depositTran);
    }

    // TRANSFER AMOUNT FROM ONE ACCOUNT TO ANOTHER
    @Transactional
    @PostMapping("/transfer")
    public ResponseEntity<?> transferAmount(@RequestBody Transfer transfer) {
        return transactionService.transferAmount(transfer);
    }

    // get transaction by tracking code
    @GetMapping("/{trackingCode}")
    public ResponseEntity<?> getTransaction(@PathVariable Long trackingCode) {
        return transactionService.getTransactions(trackingCode);
    }

    // transaction history
    @GetMapping("/history")
    public ResponseEntity<Page<Transaction>> getTransactionHistory(
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String sourceAccountNumber,
            @RequestParam(required = false) String destinationAccountNumber,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return transactionService.getTransactionsHistory(accountNumber,
                sourceAccountNumber,
                destinationAccountNumber,
                transactionType,
                minAmount,
                maxAmount,
                fromDate,
                toDate,
                page,
                size,
                sortBy,
                sortDir);
    }
}
