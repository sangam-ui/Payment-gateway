package org.example.repository;

import org.example.model.BankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccountEntity, Long> {

    List<BankAccountEntity> findByPhoneOrderByIdDesc(String phone);

    Optional<BankAccountEntity> findByIdAndPhone(Long id, String phone);
}

