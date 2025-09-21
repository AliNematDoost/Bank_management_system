package com.example.mohaimen.Repository;

import com.example.mohaimen.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    boolean existsAccountByNationalId(String nationalId);
    boolean existsAccountByAccountNumber(String accountNumber);
}
