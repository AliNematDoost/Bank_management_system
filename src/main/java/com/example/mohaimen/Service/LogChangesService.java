package com.example.mohaimen.Service;

import com.example.mohaimen.Repository.AccountChangeLogRepository;
import com.example.mohaimen.model.Account;
import com.example.mohaimen.model.AccountChangeLogs;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class LogChangesService {

    private final AccountChangeLogRepository accountChangeLogRepository;
    public LogChangesService(AccountChangeLogRepository accountChangeLogRepository) {
        this.accountChangeLogRepository = accountChangeLogRepository;
    }

    public Date logChange(Account oldAccount, Account newAccount) {
        String currentUser = getCurrentUser();
        Date changeDate = null;

        // national ID
        if (!oldAccount.getNationalId().equals(newAccount.getNationalId())) {
            AccountChangeLogs accountChangeLogs = new AccountChangeLogs(
                    oldAccount.getAccountNumber(),
                    "nationalId",
                    oldAccount.getNationalId(),
                    newAccount.getNationalId(),
                    currentUser,
                    new Date()
            );
            accountChangeLogRepository.save(accountChangeLogs);
            changeDate = new Date();
        }

        // customer name
        if (!oldAccount.getCustomerName().equals(newAccount.getCustomerName())) {
            AccountChangeLogs accountChangeLogs = new AccountChangeLogs(
                    oldAccount.getAccountNumber(),
                    "customerName",
                    oldAccount.getCustomerName(),
                    newAccount.getCustomerName(),
                    currentUser,
                    new Date()
            );
            accountChangeLogRepository.save(accountChangeLogs);
            changeDate = new Date();
        }

        // birthday
        if (!oldAccount.getBirthDate().equals(newAccount.getBirthDate())) {
            AccountChangeLogs accountChangeLogs = new AccountChangeLogs(
                    oldAccount.getAccountNumber(),
                    "birthDate",
                    oldAccount.getBirthDate().toString(),
                    newAccount.getBirthDate().toString(),
                    currentUser,
                    new Date()
            );
            accountChangeLogRepository.save(accountChangeLogs);
            changeDate = new Date();
        }

        // Customer Type
        if (!oldAccount.getCustomerType().equals(newAccount.getCustomerType())) {
            AccountChangeLogs accountChangeLogs = new AccountChangeLogs(
                    oldAccount.getAccountNumber(),
                    "customerType",
                    oldAccount.getCustomerType().toString(),
                    newAccount.getCustomerType().toString(),
                    currentUser,
                    new Date()
            );
            accountChangeLogRepository.save(accountChangeLogs);
            changeDate = new Date();
        }

        // phoneNumber
        if (!oldAccount.getPhoneNumber().equals(newAccount.getPhoneNumber())) {
            AccountChangeLogs accountChangeLogs = new AccountChangeLogs(
                    oldAccount.getAccountNumber(),
                    "phoneNumber",
                    oldAccount.getPhoneNumber(),
                    newAccount.getPhoneNumber(),
                    currentUser,
                    new Date()
            );
            accountChangeLogRepository.save(accountChangeLogs);
            changeDate = new Date();
        }

        // address
        if (!oldAccount.getAddress().equals(newAccount.getAddress())) {
            AccountChangeLogs accountChangeLogs = new AccountChangeLogs(
                    oldAccount.getAccountNumber(),
                    "address",
                    oldAccount.getAddress(),
                    newAccount.getAddress(),
                    currentUser,
                    new Date()
            );
            accountChangeLogRepository.save(accountChangeLogs);
            changeDate = new Date();
        }

        // postalCode
        if (!oldAccount.getPostalCode().equals(newAccount.getPostalCode())) {
            AccountChangeLogs accountChangeLogs = new AccountChangeLogs(
                    oldAccount.getAccountNumber(),
                    "postalCode",
                    oldAccount.getPostalCode(),
                    newAccount.getPostalCode(),
                    currentUser,
                    new Date()
            );
            accountChangeLogRepository.save(accountChangeLogs);
            changeDate = new Date();
        }

        // account status
        if (!oldAccount.getAccountStatus().equals(newAccount.getAccountStatus())) {
            AccountChangeLogs accountChangeLogs = new AccountChangeLogs(
                    oldAccount.getAccountNumber(),
                    "accountStatus",
                    oldAccount.getAccountStatus().toString(),
                    newAccount.getAccountStatus().toString(),
                    currentUser,
                    new Date()
            );
            accountChangeLogRepository.save(accountChangeLogs);
            changeDate = new Date();
        }

        return changeDate;
    }

    public String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        } else {
            return "SYSTEM USER";
        }
    }
}
