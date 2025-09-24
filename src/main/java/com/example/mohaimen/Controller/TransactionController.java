package com.example.mohaimen.Controller;

import com.example.mohaimen.Repository.AccountRepository;
import com.example.mohaimen.Repository.TransactionRepository;
import com.example.mohaimen.Service.TransactionFeeCalculator;
import com.example.mohaimen.Service.TransactionQuery;
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

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionFeeCalculator transactionFeeCalculator;

    public TransactionController(AccountRepository accountRepository,
                                 TransactionRepository transactionRepository,
                                 TransactionFeeCalculator transactionFeeCalculator) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionFeeCalculator = transactionFeeCalculator;
    }

    // DEPOSIT AMOUNT TO AN ACCOUNT
    @Transactional
    @PostMapping("")
    public ResponseEntity<?> depositOrWithdraw(@RequestBody DepositTran depositTran) {
        BigDecimal amount = depositTran.getAmount();
        String accountNumber = depositTran.getAccountNumber();

        Optional<Account> optAccount = accountRepository.findByAccountNumber(accountNumber);

        // account not found
        if (optAccount.isEmpty()) {
            Transaction transaction = new Transaction(depositTran.getTransactionType(),
                    null,
                    null,
                    amount,
                    new Date());
            transaction.setStatus(Status.FAILED);
            transactionRepository.save(transaction);
            return ResponseEntity.badRequest().body("Account not found!");
        }

        Account account = optAccount.get();

        // transaction type not valid
        if (depositTran.getTransactionType() == null) {
            Transaction transaction = new Transaction(TransactionType.INVALID,
                    null,
                    null,
                    amount,
                    new Date());
            transaction.setStatus(Status.FAILED);
            transactionRepository.save(transaction);
            return ResponseEntity.badRequest().body("Transaction type not valid!");
        }

        if (depositTran.getTransactionType().equals(TransactionType.DEPOSIT)) {
            account.setBalance(account.getBalance().add(amount));
            accountRepository.save(account);

            Transaction transaction1 = new Transaction(TransactionType.DEPOSIT,
                    transactionFeeCalculator.getBankAccountNumber(),
                    accountNumber,
                    amount,
                    new Date());
            transaction1.setStatus(Status.SUCCESS);
            transactionRepository.save(transaction1);
            return ResponseEntity.ok(transaction1);
        } else if (depositTran.getTransactionType().equals(TransactionType.WITHDRAW)) {
            if (account.getBalance().compareTo(amount) < 0) {
                Transaction transaction = new Transaction(TransactionType.WITHDRAW,
                        accountNumber,
                        transactionFeeCalculator.getBankAccountNumber(),
                        amount,
                        new Date());
                transaction.setStatus(Status.FAILED);
                transactionRepository.save(transaction);
                return ResponseEntity.badRequest().body("Not enough balance in account!");
            }

            account.setBalance(account.getBalance().subtract(amount));
            accountRepository.save(account);

            Transaction transaction2 = new Transaction(TransactionType.WITHDRAW,
                    accountNumber,
                    transactionFeeCalculator.getBankAccountNumber(),
                    amount,
                    new Date());
            transaction2.setStatus(Status.SUCCESS);
            transactionRepository.save(transaction2);
            return ResponseEntity.ok(transaction2);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    // TRANSFER AMOUNT FROM ONE ACCOUNT TO ANOTHER
    @Transactional
    @PostMapping("/transfer")
    public ResponseEntity<?> transferAmount(@RequestBody Transfer transfer) {

        // check if accounts exist
        Optional<Account> optFromAccount = accountRepository.findByAccountNumber(transfer.getFromAccount());
        Optional<Account> optToAccount = accountRepository.findByAccountNumber(transfer.getToAccount());
        Account fromAccount = optFromAccount.orElse(null);
        Account toAccount = optToAccount.orElse(null);

        if (optFromAccount.isEmpty() || optToAccount.isEmpty()) {
            Transaction transaction = new Transaction(TransactionType.TRANSFER,
                    fromAccount != null ? fromAccount.getAccountNumber() : null,
                    toAccount != null ? toAccount.getAccountNumber() : null,
                    transfer.getAmount(),
                    new Date());

            transaction.setStatus(Status.FAILED);
            transactionRepository.save(transaction);
            return ResponseEntity.badRequest().body("One or both accounts not found!");
        }

        // check if fromAccount and toAccount are different
        if (transfer.getFromAccount().equals(transfer.getToAccount())) {
            Transaction transaction = new Transaction(TransactionType.TRANSFER,
                    optFromAccount.get().getAccountNumber(),
                    optToAccount.get().getAccountNumber(),
                    transfer.getAmount(),
                    new Date());
            transaction.setStatus(Status.FAILED);
            transactionRepository.save(transaction);
            return ResponseEntity.badRequest().body("From and To account cannot be the same!");
        }

        // check if fromAccount and toAccount are active
        if (fromAccount.getAccountStatus().equals(AccountStatus.INACTIVE) ||
                toAccount.getAccountStatus().equals(AccountStatus.INACTIVE) ||
                fromAccount.getAccountStatus().equals(AccountStatus.BLOCKED) ||
                toAccount.getAccountStatus().equals(AccountStatus.BLOCKED)) {

            Transaction transaction = new Transaction(TransactionType.TRANSFER,
                    fromAccount.getAccountNumber(),
                    toAccount.getAccountNumber(),
                    transfer.getAmount(),
                    new Date());
            transaction.setStatus(Status.FAILED);
            transactionRepository.save(transaction);
            return ResponseEntity.badRequest().body("From and To account must be active!");
        }

        // check if fromAccount has sufficient balance
        BigDecimal amount = transfer.getAmount();
        // calculate transaction fee
        BigDecimal fee = transactionFeeCalculator.CalculateFee(amount);

        if (fromAccount.getBalance().compareTo(amount.add(fee)) < 0) {
            Transaction transaction = new Transaction(TransactionType.TRANSFER,
                    fromAccount.getAccountNumber(),
                    toAccount.getAccountNumber(),
                    transfer.getAmount(),
                    new Date());
            transaction.setStatus(Status.FAILED);
            transactionRepository.save(transaction);
            return ResponseEntity.badRequest().body("Not enough balance in from account!");
        }

        // transfer amount
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // create transaction record for transfer
        Transaction transaction = new Transaction(TransactionType.TRANSFER,
                fromAccount.getAccountNumber(),
                toAccount.getAccountNumber(),
                amount,
                new Date());
        transaction.setStatus(Status.SUCCESS);
        transactionRepository.save(transaction);

        // create transaction for transaction fee
        fromAccount.setBalance(fromAccount.getBalance().subtract(fee));
        accountRepository.save(fromAccount);

        Transaction TransactionFee = new Transaction(TransactionType.FEE,
                fromAccount.getAccountNumber(),
                transactionFeeCalculator.getBankAccountNumber(),
                fee,
                new Date());
        TransactionFee.setStatus(Status.SUCCESS);
        transactionRepository.save(TransactionFee);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/{trackingCode}")
    public ResponseEntity<?> getTransaction(@PathVariable Long trackingCode) {
        Optional<Transaction> transaction = transactionRepository.findById(trackingCode);

        // tracking code in not found
        if (transaction.isEmpty()) {
            return ResponseEntity.badRequest().body("Transaction not found!");
        }

        // transaction is FAILED or SUCCESS
        if (transaction.get().getStatus().equals(Status.FAILED)) {
            return ResponseEntity.badRequest().body("Transaction is failed at " + transaction.get().getDate());
        } else {
            return ResponseEntity.ok(transaction.get());
        }
    }

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
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Transaction> spec = TransactionQuery.filter(
                accountNumber,
                sourceAccountNumber,
                destinationAccountNumber,
                transactionType,
                minAmount,
                maxAmount,
                fromDate,
                toDate
        );

        Page<Transaction> transactions = transactionRepository.findAll(spec, pageable);
        return ResponseEntity.ok(transactions);
    }
}
