package com.example.mohaimen.Repository;

import com.example.mohaimen.model.AccountChangeLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountChangeLogRepository extends JpaRepository<AccountChangeLogs, Long> {
}
