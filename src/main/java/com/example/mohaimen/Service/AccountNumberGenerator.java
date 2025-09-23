package com.example.mohaimen.Service;

import com.example.mohaimen.Repository.AccountRepository;

import java.util.concurrent.ThreadLocalRandom;

@org.springframework.stereotype.Service
public class AccountNumberGenerator {

    private final AccountRepository accountRepository;
    public AccountNumberGenerator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public String GenerateUniqueNumberfunction() {
        final long min = 10_000_000_000_000L;
        final long max = 99_999_999_999_999L;

        while (true) {
            long candidate = ThreadLocalRandom.current().nextLong(min, max + 1);
            String candidateStr = Long.toString(candidate);
            if (!accountRepository.existsAccountByAccountNumber(candidateStr)) {
                return candidateStr;
            }
        }
    }
}
