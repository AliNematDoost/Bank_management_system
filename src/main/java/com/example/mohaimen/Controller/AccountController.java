package com.example.mohaimen.Controller;

import com.example.mohaimen.Repository.AccountChangeLogRepository;
import com.example.mohaimen.Repository.AccountRepository;
import com.example.mohaimen.Service.AccountNumberGenerator;
import com.example.mohaimen.Service.AccountService;
import com.example.mohaimen.Service.LogChangesService;
import com.example.mohaimen.model.Account;
import com.example.mohaimen.model.AccountStatus;
import com.example.mohaimen.model.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/create/account/")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {

        this.accountService = accountService;
    }

    // JSON ARRAY OF ALL USERS
    @GetMapping("/all")
    public Account[] getAllAccounts() {

        return accountService.getAllAccounts();
    }

    // CREATE A NEW ACCOUNT
    @PostMapping("")
    public ResponseEntity<?> createAccount(@RequestBody Customer customer) {

        return accountService.createAccountService(customer);
    }

    // UPDATE AN ACCOUNT
    // just update fields you want and set other fields to NULL
    @PutMapping("/{nationalID}")
    public ResponseEntity<?> UpdateAccount(@PathVariable String nationalID, @RequestBody Account newAccountData) {

        return accountService.updateAccountService(nationalID, newAccountData);
    }

    // RETRIEVE AN ACCOUNT BY ACCOUNT NUMBER
    @GetMapping("/accountNumber/{accountNumber}" )
    public ResponseEntity<?> getAccount(@PathVariable String accountNumber) {

        return accountService.getAccountByAccountNumber(accountNumber);
    }

    // get account number by national id
    @GetMapping("/nationalID/{nationalID}" )
    public ResponseEntity<?> getAccountNumber(@PathVariable String nationalID) {

        return accountService.getAccountNumberByNationalId(nationalID);
    }

    // get the account balance by account number
    @GetMapping("/balance/{accountNumber}" )
    public ResponseEntity<?> getAccountBalance(@PathVariable String accountNumber) {

        return accountService.getBalanceByAccountNumber(accountNumber);
    }
}

