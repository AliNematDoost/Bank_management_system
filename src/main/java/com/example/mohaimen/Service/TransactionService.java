package com.example.mohaimen.Service;

import com.example.mohaimen.Repository.AccountRepository;
import com.example.mohaimen.Repository.TransactionRepository;
import com.example.mohaimen.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionFeeCalculator transactionFeeCalculator;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              TransactionFeeCalculator transactionFeeCalculator,
                              AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionFeeCalculator = transactionFeeCalculator;
        this.accountRepository = accountRepository;
    }


    // deposit or withdraw to/from an account
    public ResponseEntity<?> depositOrWithdrawal(DepositTran depositTran) {
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
            return ResponseEntity.notFound().build();
        }

        Account account = optAccount.get();

        // transaction type == DEPOSIT
        if (depositTran.getTransactionType().equals(TransactionType.DEPOSIT)) {

            if (account.getAccountStatus().equals(AccountStatus.ACTIVE)) {

                account.setBalance(account.getBalance().add(amount));
                accountRepository.save(account);

                Transaction transaction1 = new Transaction(TransactionType.DEPOSIT,
                        transactionFeeCalculator.getBankAccountNumber(),
                        accountNumber,
                        amount,
                        new Date());
                transaction1.setStatus(Status.SUCCESS);
                transactionRepository.save(transaction1);

                Map<String, Object> response = Map.of(
                        "message", "Deposit successful",
                        "transaction", transaction1
                );
                return ResponseEntity.ok(response);

            } else {
                Transaction transaction = new Transaction(TransactionType.DEPOSIT,
                        transactionFeeCalculator.getBankAccountNumber(),
                        accountNumber,
                        amount,
                        new Date());
                transaction.setStatus(Status.FAILED);
                transactionRepository.save(transaction);

                Map<String, Object> response = Map.of(
                        "message", "Account is not active",
                        "accountStatus", account.getAccountStatus()
                );
                return ResponseEntity.badRequest().body(response);
            }

        // transaction type == WITHDRAW
        } else if (depositTran.getTransactionType().equals(TransactionType.WITHDRAW)) {

            // check if account is active
            if (account.getAccountStatus().equals(AccountStatus.ACTIVE)) {

                if (account.getBalance().compareTo(amount) < 0) {
                    Transaction transaction = new Transaction(TransactionType.WITHDRAW,
                            accountNumber,
                            transactionFeeCalculator.getBankAccountNumber(),
                            amount,
                            new Date());
                    transaction.setStatus(Status.FAILED);
                    transactionRepository.save(transaction);

                    Map<String, Object> response = Map.of(
                            "message", "Not enough balance in account!",
                            "accountBalance", account.getBalance(),
                            "withdrawAmount", amount
                    );
                    return ResponseEntity.badRequest().body(response);
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

                Map<String, Object> response = Map.of(
                        "message", "Withdrawal successful",
                        "transaction", transaction2
                );
                return ResponseEntity.ok(transaction2);

            } else {
                Transaction transaction = new Transaction(TransactionType.WITHDRAW,
                        accountNumber,
                        transactionFeeCalculator.getBankAccountNumber(),
                        amount,
                        new Date());
                transaction.setStatus(Status.FAILED);
                transactionRepository.save(transaction);

                Map<String, Object> response = Map.of(
                        "message", "Account is not active",
                        "accountStatus", account.getAccountStatus()
                );
                return ResponseEntity.badRequest().body(response);
            }
        } else {
            Transaction transaction = new Transaction(TransactionType.INVALID,
                    null,
                    null,
                    amount,
                    new Date());
            transaction.setStatus(Status.FAILED);
            transactionRepository.save(transaction);

            Map<String, Object> response = Map.of(
                    "message", "Transaction type not valid",
                    "transactionType", depositTran.getTransactionType()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }


    // Transfer from an account to another one
    public ResponseEntity<?> transferAmount(Transfer transfer) {

        // check if accounts exist
        Optional<Account> optFromAccount = accountRepository.findByAccountNumber(transfer.getFromAccount());
        Optional<Account> optToAccount = accountRepository.findByAccountNumber(transfer.getToAccount());
        Account fromAccount = optFromAccount.orElse(null);
        Account toAccount = optToAccount.orElse(null);
        boolean fromAccountExists = optFromAccount.isPresent();
        boolean toAccountExists = optToAccount.isPresent();

        // one or both accounts not found
        if (optFromAccount.isEmpty() || optToAccount.isEmpty()) {
            Transaction transaction = new Transaction(TransactionType.TRANSFER,
                    fromAccount != null ? fromAccount.getAccountNumber() : null,
                    toAccount != null ? toAccount.getAccountNumber() : null,
                    transfer.getAmount(),
                    new Date());

            transaction.setStatus(Status.FAILED);
            transactionRepository.save(transaction);

            String message;
            if (!fromAccountExists && !toAccountExists) {
                message = "Both accounts not found!";
            } else if (!fromAccountExists) {
                message = "From account not found!";
            } else {
                message = "To account not found!";
            }
            Map<String, String> response = Map.of(
                    "message", message,
                    "FromAccountNumber", transfer.getFromAccount(),
                    "ToAccountNumber", transfer.getToAccount()
            );
            return ResponseEntity.badRequest().body(response);
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

            Map<String, Object> response = Map.of(
                    "message", "From and To account cannot be the same!",
                    "FromAccountNumber", transfer.getFromAccount(),
                    "ToAccountNumber", transfer.getToAccount()
            );
            return ResponseEntity.badRequest().body(response);
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

            Map<String, Object> response = Map.of(
                    "message", "From and To account must be active!",
                    "fromAccountStatus", fromAccount.getAccountStatus(),
                    "toAccountStatus", toAccount.getAccountStatus()
            );
            return ResponseEntity.badRequest().body(response);
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

            Map<String, Object> response = Map.of(
                    "message", "Not enough balance in from account!",
                    "fromAccountBalance", fromAccount.getBalance(),
                    "transferAmount", amount,
                    "transactionFee", fee,
                    "total", amount.add(fee)
            );
            return ResponseEntity.badRequest().body(response);
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

        Map<String, Object> response = Map.of(
                "message", "Transfer successful",
                "transferTransaction", transaction,
                "feeTransaction", TransactionFee
        );
        return ResponseEntity.ok(response);
    }


    // get transaction by tracking code
    public ResponseEntity<?> getTransactions(Long trackingCode) {
        Optional<Transaction> transaction = transactionRepository.findById(trackingCode);

        // tracking code in not found
        if (transaction.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // transaction is FAILED or SUCCESS
        if (transaction.get().getStatus().equals(Status.FAILED)) {

            Map<String, Object> response = Map.of(
                    "message", "Transaction is FAILED",
                    "transaction", transaction.get()
            );
            return ResponseEntity.badRequest().body(response);
        } else {

            Map<String, Object> response = Map.of(
                    "message", "Transaction is SUCCESSFUL",
                    "transaction", transaction.get()
            );
            return ResponseEntity.ok(response);
        }
    }


    // transaction history
    public ResponseEntity<?> getTransactionsHistory(String accountNumber,
                                                                    String sourceAccountNumber,
                                                                    String destinationAccountNumber,
                                                                    TransactionType transactionType,
                                                                    BigDecimal minAmount,
                                                                    BigDecimal maxAmount,
                                                                    Date fromDate,
                                                                    Date toDate,
                                                                    int page,
                                                                    int size,
                                                                    String sortBy,
                                                                    String sortDir) {
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

        Map<String, Object> response = Map.of(
                "message", "Transaction history fetched successfully",
                "transactions", transactions
        );
        return ResponseEntity.ok(response);
    }
}
