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

        accountRepository.save(account);
        return ResponseEntity.ok("Account created with account number: " + service.GenerateUniqueNumberfunction());
    }

//    @PutMapping()
}

