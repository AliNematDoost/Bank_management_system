package com.example.mohaimen.Controller;

import com.example.mohaimen.Repository.AccountRepository;
import com.example.mohaimen.Repository.TransactionRepository;
import com.example.mohaimen.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/Transaction")
public class TransactionController {

    final BigDecimal transactionFee = BigDecimal.valueOf(720.00);

    final AccountRepository accountRepository;
    final TransactionRepository transactionRepository;

    public TransactionController(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // DEPOSIT AMOUNT TO AN ACCOUNT
    @PostMapping("")
    public ResponseEntity<?> depositOrWithdraw(@RequestBody DepositTran depositTran) {
        BigDecimal amount = depositTran.getAmount();
        String accountNumber = depositTran.getAccountNumber();

        Optional<Account> optAccount = accountRepository.findByAccountNumber(accountNumber);

        // account not found
        if (optAccount.isEmpty()) {
            Transaction transaction = new Transaction(depositTran.getTransactionType(), amount, new Date());
            transaction.setStatus(Status.FAILED);
            transactionRepository.save(transaction);
            return ResponseEntity.badRequest().body("Account not found!");
        }

        Account account = optAccount.get();

        // transaction type not valid
        if (depositTran.getTransactionType() == null) {
            Transaction transaction = new Transaction(TransactionType.INVALID, amount, new Date());
            transaction.setStatus(Status.FAILED);
            transactionRepository.save(transaction);
            return ResponseEntity.badRequest().body("Transaction type not valid!");
        }

        if (depositTran.getTransactionType().equals(TransactionType.DEPOSIT)) {
            account.setBalance(account.getBalance().add(amount).subtract(transactionFee));
            accountRepository.save(account);

            Transaction transaction1 = new Transaction(TransactionType.DEPOSIT, amount, new Date());
            transaction1.setStatus(Status.SUCCESS);
            transactionRepository.save(transaction1);
            return ResponseEntity.ok(transaction1);
        } else if (depositTran.getTransactionType().equals(TransactionType.WITHDRAW)) {
            if (account.getBalance().compareTo(amount.add(transactionFee)) < 0) {
                Transaction transaction = new Transaction(TransactionType.WITHDRAW,
                        depositTran.getAmount(),
                        new Date());
                transaction.setStatus(Status.FAILED);
                transactionRepository.save(transaction);
                return ResponseEntity.badRequest().body("Not enough balance in account!");
            }

            account.setBalance(account.getBalance().subtract(amount).subtract(transactionFee));
            accountRepository.save(account);

            Transaction transaction2 = new Transaction(TransactionType.WITHDRAW, amount, new Date());
            transaction2.setStatus(Status.SUCCESS);
            transactionRepository.save(transaction2);
            return ResponseEntity.ok(transaction2);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    // TRANSFER AMOUNT FROM ONE ACCOUNT TO ANOTHER
    @PostMapping("/transfer")
    public ResponseEntity<?> transferAmount(@RequestBody Transfer transfer) {

        // check if accounts exist
        Optional<Account> optFromAccount = accountRepository.findByAccountNumber(transfer.getFromAccount());
        Optional<Account> optToAccount = accountRepository.findByAccountNumber(transfer.getToAccount());

        if (optFromAccount.isEmpty() || optToAccount.isEmpty()) {
            Transaction transaction = new Transaction(TransactionType.TRANSFER,
                    transfer.getAmount(),
                    new Date());
            transaction.setStatus(Status.FAILED);
            transactionRepository.save(transaction);
            return ResponseEntity.badRequest().body("One or both accounts not found!");
        }

        // check if fromAccount and toAccount are different
        if (transfer.getFromAccount().equals(transfer.getToAccount())) {
            Transaction transaction = new Transaction(TransactionType.TRANSFER,
                    transfer.getAmount(),
                    new Date());
            transaction.setStatus(Status.FAILED);
            transactionRepository.save(transaction);
            return ResponseEntity.badRequest().body("From and To account cannot be the same!");
        }

        // check if fromAccount and toAccount are active
        Account fromAccount = optFromAccount.get();
        Account toAccount = optToAccount.get();
        if (fromAccount.getAccountStatus().equals(AccountStatus.INACTIVE) ||
                toAccount.getAccountStatus().equals(AccountStatus.INACTIVE)) {
            Transaction transaction = new Transaction(TransactionType.TRANSFER,
                    transfer.getAmount(),
                    new Date());
            transaction.setStatus(Status.FAILED);
            transactionRepository.save(transaction);
            return ResponseEntity.badRequest().body("From and To account must be active!");
        }

        // check if fromAccount has sufficient balance
        BigDecimal amount = transfer.getAmount();
        if (fromAccount.getBalance().compareTo(amount.add(transactionFee)) < 0) {
            Transaction transaction = new Transaction(TransactionType.TRANSFER,
                    transfer.getAmount(),
                    new Date());
            transaction.setStatus(Status.FAILED);
            transactionRepository.save(transaction);
            return ResponseEntity.badRequest().body("Not enough balance in from account!");
        }

        // transfer amount
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount).subtract(transactionFee));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = new Transaction(TransactionType.TRANSFER, amount, new Date());
        transaction.setStatus(Status.SUCCESS);
        transactionRepository.save(transaction);
        return ResponseEntity.ok(transaction);
    }


}
