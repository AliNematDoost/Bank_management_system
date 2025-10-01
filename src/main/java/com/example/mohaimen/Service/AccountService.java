package com.example.mohaimen.Service;

import com.example.mohaimen.Repository.AccountRepository;
import com.example.mohaimen.model.Account;
import com.example.mohaimen.model.AccountStatus;
import com.example.mohaimen.model.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final LogChangesService logChangesService;

    public AccountService(AccountRepository accountRepository,
                          AccountNumberGenerator accountNumberGenerator,
                          LogChangesService logChangesService) {
        this.accountRepository = accountRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.logChangesService = logChangesService;
    }

    // create account service
    public ResponseEntity<?> createAccountService(Customer customer) {

        // preprocessing and validation
        if (customer.getCustomerName() == null || customer.getCustomerName().isEmpty() ||
                customer.getNationalId() == null || customer.getNationalId().isEmpty() ||
                customer.getBirthDate() == null || customer.getBirthDate().toString().isEmpty() ||
                customer.getCustomerType() == null || customer.getCustomerType().toString().isEmpty() ||
                customer.getPhoneNumber() == null || customer.getPhoneNumber().isEmpty() ||
                customer.getAddress() == null || customer.getAddress().isEmpty() ||
                customer.getPostalCode() == null || customer.getPostalCode().isEmpty()) {

            Map<String, Object> response = Map.of(
                    "message", "All fields are required!",
                    "providedData", customer
            );
            return ResponseEntity.badRequest().body(response);
        }

        // Account with the same national ID already exists
        if (accountRepository.existsAccountByNationalId(customer.getNationalId())) {

            Account accountByNationalId = accountRepository.findAccountByNationalId(customer.getNationalId());
            Map<String, Object> response = Map.of(
                    "message", "Account for this national ID has already been made!",
                    "Account", accountByNationalId
            );
            return ResponseEntity.badRequest().body(response);
        }

        final String accountNumber = accountNumberGenerator.GenerateUniqueNumberfunction();
        Account account = new Account(
                customer.getNationalId(),
                customer.getCustomerName(),
                accountNumber,
                customer.getBirthDate(),
                customer.getCustomerType(),
                customer.getPhoneNumber(),
                customer.getAddress(),
                customer.getPostalCode(),
                AccountStatus.ACTIVE,
                new Date(),
                java.math.BigDecimal.ZERO,
                null
        );

        accountRepository.save(account);
        Map<String, Object> response = Map.of(
                "message", "Account created successfully!",
                "account", account
        );
        return ResponseEntity.ok(response);
    }


    // update account service
    public ResponseEntity<?> updateAccountService(String nationalID,
                                                       Account newAccountData) {

        Account currentAccount = accountRepository.findById(nationalID).orElse(null);

        // accountNumber not found
        if (currentAccount == null) {

            Map<String, Object> response = Map.of(
                    "message", "Account not found for entered national ID! \nCreate Account first.",
                    "national ID", nationalID
            );
            return ResponseEntity.badRequest().body(response);
        }

        // Save old account values
        Account oldAccount = new Account(currentAccount.getNationalId(),
                currentAccount.getCustomerName(),
                currentAccount.getAccountNumber(),
                currentAccount.getBirthDate(),
                currentAccount.getCustomerType(),
                currentAccount.getPhoneNumber(),
                currentAccount.getAddress(),
                currentAccount.getPostalCode(),
                currentAccount.getAccountStatus(),
                currentAccount.getAccountCreationDate(),
                currentAccount.getBalance(),
                currentAccount.getLastUpdated()
        );

        // Check if another account with the new national ID already exists
        if (!nationalID.equals(newAccountData.getNationalId())) {
            if (accountRepository.existsAccountByNationalId(newAccountData.getNationalId())) {

                Map<String, Object> response = Map.of(
                        "message", "Another account with this national ID already exists!",
                        "national ID", newAccountData.getNationalId()
                );
                return ResponseEntity.badRequest().body(response);
            }

            currentAccount.setNationalId(newAccountData.getNationalId());
        }

        if (newAccountData.getCustomerName() != null
                && !newAccountData.getCustomerName().isEmpty()) {

            currentAccount.setCustomerName(newAccountData.getCustomerName());
        }

        if (newAccountData.getBirthDate() != null
                && !newAccountData.getBirthDate().toString().isEmpty()) {

            currentAccount.setBirthDate(newAccountData.getBirthDate());
        }

        if (newAccountData.getCustomerType() != null
                && !newAccountData.getCustomerType().toString().isEmpty()) {

            currentAccount.setCustomerType(newAccountData.getCustomerType());
        }

        if (newAccountData.getPhoneNumber() != null
                && !newAccountData.getPhoneNumber().isEmpty()) {

            currentAccount.setPhoneNumber(newAccountData.getPhoneNumber());
        }

        if (newAccountData.getAddress() != null
                && !newAccountData.getAddress().isEmpty()) {

            currentAccount.setAddress(newAccountData.getAddress());
        }

        if (newAccountData.getPostalCode() != null
                && !newAccountData.getPostalCode().isEmpty()) {

            currentAccount.setPostalCode(newAccountData.getPostalCode());
        }

        if (newAccountData.getAccountStatus() != null
                && !newAccountData.getAccountStatus().toString().isEmpty()) {

            currentAccount.setAccountStatus(newAccountData.getAccountStatus());
        }

        Date lastChangeDate = logChangesService.logChange(oldAccount, currentAccount);

        if (lastChangeDate != null) {
            currentAccount.setLastUpdated(lastChangeDate);
        }

        accountRepository.save(currentAccount);

        Map<String, Object> response = Map.of(
                "message", "Account updated successfully!",
                "account", currentAccount
        );
        return ResponseEntity.ok(response);
    }


    // find account by account number
    public ResponseEntity<?> getAccountByAccountNumber(String accountNumber) {
        List<Account> account = accountRepository.findAllByAccountNumber(accountNumber);

        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            Map<String, Object> response = Map.of(
                    "message", "Account found successfully!",
                    "account", account.get(0)
            );
            return ResponseEntity.ok(response);
        }
    }


    // get account number by national id
    public ResponseEntity<?> getAccountNumberByNationalId(String nationalID) {
        Account account = accountRepository.findById(nationalID).orElse(null);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, String> response = Map.of(
                "message", "Account number fetched successfully!",
                "accountNumber", account.getAccountNumber()
        );
        return ResponseEntity.ok(response);
    }


    // get balance by account number
    public ResponseEntity<?> getBalanceByAccountNumber(String accountNumber) {

        List<Account> account = accountRepository.findAllByAccountNumber(accountNumber);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            Map<String, Object> response = Map.of(
                    "message", "Account balance fetched successfully!",
                    "balance", account.get(0).getBalance()
            );
            return ResponseEntity.ok(response);
        }
    }


    // get all accounts
    public Account[] getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.toArray(new Account[0]);
    }
}
