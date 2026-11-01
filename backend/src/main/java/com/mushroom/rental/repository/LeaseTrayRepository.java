package com.mushroom.rental.repository;

import com.mushroom.rental.entity.LeaseStatus;
import com.mushroom.rental.entity.LeaseTray;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaseTrayRepository extends JpaRepository<LeaseTray, UUID> {

    @Query("SELECT lt FROM LeaseTray lt " +
            "JOIN FETCH lt.lease l " +
            "JOIN FETCH lt.tray t " +
            "WHERE lt.linkCode = :linkCode")
    Optional<LeaseTray> findByLinkCodeWithDetails(@Param("linkCode") String linkCode);

    List<LeaseTray> findByLeaseId(UUID leaseId);

    @Query("SELECT lt FROM LeaseTray lt " +
            "JOIN FETCH lt.tray t " +
            "JOIN FETCH t.room r " +
            "JOIN FETCH lt.lease l " +
            "WHERE l.customerId = :customerId AND l.status = :status")
    List<LeaseTray> findCustomerActiveTrays(
            @Param("customerId") UUID customerId,
            @Param("status") LeaseStatus status
    );

    @Query("SELECT lt FROM LeaseTray lt " +
            "JOIN FETCH lt.tray t " +
            "JOIN FETCH t.room r " +
            "JOIN FETCH lt.lease l " +
            "WHERE lt.trayId = :trayId AND l.customerId = :customerId AND l.status = 'ACTIVE'")
    Optional<LeaseTray> findCustomerActiveTray(
            @Param("trayId") UUID trayId,
            @Param("customerId") UUID customerId
    );

    @Query("SELECT COUNT(lt) > 0 FROM LeaseTray lt " +
            "JOIN lt.lease l " +
            "WHERE lt.trayId = :trayId AND l.status = :status")
    boolean existsByTrayIdAndLeaseStatus(
            @Param("trayId") UUID trayId,
            @Param("status") LeaseStatus status
    );
}
