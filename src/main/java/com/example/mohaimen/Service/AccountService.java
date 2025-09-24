package com.example.mohaimen.Service;

import com.example.mohaimen.Repository.AccountRepository;
import com.example.mohaimen.model.Account;
import com.example.mohaimen.model.AccountStatus;
import com.example.mohaimen.model.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
    public ResponseEntity<String> createAccountService(Customer customer) {
        // Account with the same national ID already exists
        if (accountRepository.existsAccountByNationalId(customer.getNationalId())) {
            return ResponseEntity.badRequest().body("Account for this national ID has already been made!");
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
        return ResponseEntity.ok("Account created with account number: " + accountNumber);
    }


    // update account service
    public ResponseEntity<String> updateAccountService(String nationalID,
                                                       Account newAccountData) {

        Account currentAccount = accountRepository.findById(nationalID).orElse(null);

        // accountNumber not found
        if (currentAccount == null) {
            return ResponseEntity.badRequest().body("Account not found!");
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
                return ResponseEntity.badRequest().body("Another account with this national ID already exists!");
            }
            currentAccount.setNationalId(newAccountData.getNationalId());
        }

        if (newAccountData.getCustomerName() != null) {
            currentAccount.setCustomerName(newAccountData.getCustomerName());
        }
        if (newAccountData.getBirthDate() != null) {
            currentAccount.setBirthDate(newAccountData.getBirthDate());
        }
        if (newAccountData.getCustomerType() != null) {
            currentAccount.setCustomerType(newAccountData.getCustomerType());
        }
        if (newAccountData.getPhoneNumber() != null) {
            currentAccount.setPhoneNumber(newAccountData.getPhoneNumber());
        }
        if (newAccountData.getAddress() != null) {
            currentAccount.setAddress(newAccountData.getAddress());
        }
        if (newAccountData.getPostalCode() != null) {
            currentAccount.setPostalCode(newAccountData.getPostalCode());
        }
        if (newAccountData.getAccountStatus() != null) {
            currentAccount.setAccountStatus(newAccountData.getAccountStatus());
        }

        Date lastChangeDate = logChangesService.logChange(oldAccount, currentAccount);

        if (lastChangeDate != null) {
            currentAccount.setLastUpdated(lastChangeDate);
        }
        accountRepository.save(currentAccount);

        return ResponseEntity.ok("Account updated with account number: " +
                currentAccount.getAccountNumber());
    }


    // find account by account number
    public Account[] getAccountByAccountNumber(String accountNumber) {
        List<Account> account = accountRepository.findAllByAccountNumber(accountNumber);
        return account.toArray(new Account[0]);
    }


    // get account number by national id
    public String getAccountNumberByNationalId(String nationalID) {
        Account account = accountRepository.findById(nationalID).orElse(null);
        if (account == null) {
            return "Account not found!";
        }
        return account.getAccountNumber();
    }


    // get balance by account number
    public String getBalanceByAccountNumber(String accountNumber) {
        List<Account> account = accountRepository.findAllByAccountNumber(accountNumber);
        if (account.isEmpty()) {
            return "Account not found!";
        }
        return "Account balance: " + account.toArray(new Account[0])[0].getBalance();
    }


    // get all accounts
    public Account[] getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.toArray(new Account[0]);
    }
}
