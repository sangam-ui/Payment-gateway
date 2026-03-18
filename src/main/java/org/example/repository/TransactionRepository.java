package org.example.repository;

import org.example.model.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

    List<TransactionEntity> findBySourcePhoneOrderByCreatedAtDesc(String sourcePhone);
}
