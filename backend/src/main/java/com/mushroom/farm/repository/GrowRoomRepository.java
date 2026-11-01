package com.mushroom.farm.repository;

import com.mushroom.farm.entity.GrowRoom;
import com.mushroom.farm.entity.GrowRoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GrowRoomRepository extends JpaRepository<GrowRoom, UUID> {

    Optional<GrowRoom> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Query("SELECT r FROM GrowRoom r WHERE " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:search IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<GrowRoom> findByFilters(
            @Param("status") GrowRoomStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
