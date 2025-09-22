package com.example.mohaimen.Controller;

import com.example.mohaimen.Repository.AccountRepository;
import com.example.mohaimen.Service.Service;
import com.example.mohaimen.model.Account;
import com.example.mohaimen.model.AccountStatus;
import com.example.mohaimen.model.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/create/account/")
public class Controller {

    private final AccountRepository accountRepository;
    private final Service service;
    public Controller(AccountRepository accountRepository, Service service) {
        this.accountRepository = accountRepository;
        this.service = service;
    }

//    this is for getting all accounts
    @GetMapping("/all")
    public String getAllAccounts() {
        return accountRepository.findAll().toString();
    }

    @PostMapping("")
    public ResponseEntity<String> createAccount(@RequestBody Customer customer) {
        if (accountRepository.existsAccountByNationalId(customer.getNationalId())) {
            return ResponseEntity.badRequest().body("Account for this national ID has already been made!");
        }

        Account account = new Account();
        account.setAccountNumber(service.GenerateUniqueNumberfunction());
        account.setNationalId(customer.getNationalId());
        account.setCustomerName(customer.getCustomerName());
        account.setBirthDate(customer.getBirthDate());
        account.setCustomerType(customer.getCustomerType());
        account.setPhoneNumber(customer.getPhoneNumber());
        account.setAddress(customer.getAddress());
        account.setPostalCode(customer.getPostalCode());
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setAccountCreationDate(new java.util.Date());

        accountRepository.save(account);
        return ResponseEntity.ok("Account created with account number: " + service.GenerateUniqueNumberfunction());
    }

    // just update fields you want and set other fields to NULL
    @PutMapping("/{accountNumber}")
    public ResponseEntity<String> UpdateAccount(@PathVariable String accountNumber,
                                                @RequestBody Account newAccountData) {

        Account currentAccount = accountRepository.findById(accountNumber).orElse(null);

        // accountNumber not found
        if (currentAccount == null) {
            return ResponseEntity.badRequest().body("Account not found!");
        }

        // Check if another account with the new national ID already exists
        if (!currentAccount.getNationalId().equals(newAccountData.getNationalId())) {
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
        return ResponseEntity.ok("Account updated with account number: "+ accountNumber);
    }
}

