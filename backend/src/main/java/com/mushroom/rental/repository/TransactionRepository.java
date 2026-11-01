package com.mushroom.rental.repository;

import com.mushroom.rental.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByLeaseId(UUID leaseId);

    Optional<Transaction> findTopByLeaseIdOrderByCreatedAtDesc(UUID leaseId);
}
