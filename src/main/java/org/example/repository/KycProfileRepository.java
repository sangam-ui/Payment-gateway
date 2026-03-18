package org.example.repository;

import org.example.model.KycProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycProfileRepository extends JpaRepository<KycProfileEntity, String> {
}

