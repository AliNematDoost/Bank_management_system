package com.example.mohaimen.Controller;

import com.example.mohaimen.Repository.AccountChangeLogRepository;
import com.example.mohaimen.Repository.AccountRepository;
import com.example.mohaimen.Service.AccountNumberGenerator;
import com.example.mohaimen.Service.LogChangesService;
import com.example.mohaimen.model.Account;
import com.example.mohaimen.model.AccountStatus;
import com.example.mohaimen.model.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/create/account/")
public class AccountController {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final LogChangesService logChangesService;

    public AccountController(AccountRepository accountRepository,
                             AccountNumberGenerator accountNumberGenerator,
                             LogChangesService logChangesService) {
        this.accountRepository = accountRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.logChangesService = logChangesService;
    }

    // JSON ARRAY OF ALL USERS
    @GetMapping("/all")
    public Account[] getAllAccounts() {
        java.util.List<Account> accounts = accountRepository.findAll();
        return accounts.toArray(new Account[0]);
    }

    // CREATE A NEW ACCOUNT
    @PostMapping("")
    public ResponseEntity<String> createAccount(@RequestBody Customer customer) {
        if (accountRepository.existsAccountByNationalId(customer.getNationalId())) {
            return ResponseEntity.badRequest().body("Account for this national ID has already been made!");
        }

        Account account = new Account();
        final String accountNumber = accountNumberGenerator.GenerateUniqueNumberfunction();
        account.setAccountNumber(accountNumber);
        account.setNationalId(customer.getNationalId());
        account.setCustomerName(customer.getCustomerName());
        account.setBirthDate(customer.getBirthDate());
        account.setCustomerType(customer.getCustomerType());
        account.setPhoneNumber(customer.getPhoneNumber());
        account.setAddress(customer.getAddress());
        account.setPostalCode(customer.getPostalCode());
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setAccountCreationDate(new java.util.Date());
        account.setBalance(java.math.BigDecimal.ZERO);

        accountRepository.save(account);
        return ResponseEntity.ok("Account created with account number: " + accountNumber);
    }

    // UPDATE AN ACCOUNT
    // just update fields you want and set other fields to NULL
    @PutMapping("/{nationalID}")
    public ResponseEntity<String> UpdateAccount(@PathVariable String nationalID,
                                                @RequestBody Account newAccountData) {

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
                currentAccount.getBalance()
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

        accountRepository.save(currentAccount);
        logChangesService.logChange(oldAccount, currentAccount);
        return ResponseEntity.ok("Account updated with account number: "+ currentAccount.getAccountNumber());
    }

    // RETRIEVE AN ACCOUNT BY NATIONAL ID
    @GetMapping("/accountNumber/{accountNumber}" )
    public Account[] getAccount(@PathVariable String accountNumber) {
        List<Account> account = accountRepository.findAllByAccountNumber(accountNumber);
        return account.toArray(new Account[0]);
    }

    // get account number by national id
    @GetMapping("/nationalID/{nationalID}" )
    public String getAccountNumber(@PathVariable String nationalID) {
        Account account = accountRepository.findById(nationalID).orElse(null);
        if (account == null) {
            return "Account not found!";
        }
        return account.getAccountNumber();
    }

    // get the account balance by account number
    @GetMapping("/balance/{accountNumber}" )
    public String getAccountBalance(@PathVariable String accountNumber) {
        List<Account> account = accountRepository.findAllByAccountNumber(accountNumber);
        if (account.isEmpty()) {
            return "Account not found!";
        }
        return "Account balance: " + account.toArray(new Account[0])[0].getBalance();
    }
}

