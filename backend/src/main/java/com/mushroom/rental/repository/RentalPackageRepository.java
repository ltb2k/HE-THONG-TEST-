package com.mushroom.rental.repository;

import com.mushroom.rental.entity.PackageStatus;
import com.mushroom.rental.entity.RentalPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RentalPackageRepository extends JpaRepository<RentalPackage, UUID> {

    List<RentalPackage> findByStatus(PackageStatus status);

    @Query("SELECT p FROM RentalPackage p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<RentalPackage> findByFilters(
            @Param("status") PackageStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
