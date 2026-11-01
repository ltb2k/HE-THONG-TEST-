package com.mushroom.rental.repository;

import com.mushroom.rental.entity.Lease;
import com.mushroom.rental.entity.LeaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaseRepository extends JpaRepository<Lease, UUID> {

    @Query("SELECT l FROM Lease l " +
            "LEFT JOIN FETCH l.rentalPackage " +
            "LEFT JOIN FETCH l.customer " +
            "WHERE l.id = :id")
    Optional<Lease> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT l FROM Lease l WHERE " +
            "(:customerId IS NULL OR l.customerId = :customerId) AND " +
            "(:status IS NULL OR l.status = :status)")
    Page<Lease> findByFilters(
            @Param("customerId") UUID customerId,
            @Param("status") LeaseStatus status,
            Pageable pageable
    );

    long countByStatus(LeaseStatus status);
}
