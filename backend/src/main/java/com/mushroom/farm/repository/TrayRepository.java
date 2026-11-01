package com.mushroom.farm.repository;

import com.mushroom.farm.entity.Tray;
import com.mushroom.farm.entity.TrayStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrayRepository extends JpaRepository<Tray, UUID> {

    Optional<Tray> findByUniqueCodeIgnoreCase(String uniqueCode);

    boolean existsByUniqueCodeIgnoreCase(String uniqueCode);

    long countByRoomId(UUID roomId);

    long countByRoomIdAndStatusIn(UUID roomId, Collection<TrayStatus> statuses);

    List<Tray> findByRoomId(UUID roomId);

    @Query("SELECT t FROM Tray t WHERE " +
            "(:roomId IS NULL OR t.roomId = :roomId) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:search IS NULL OR LOWER(t.uniqueCode) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Tray> findByFilters(
            @Param("roomId") UUID roomId,
            @Param("status") TrayStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT t FROM Tray t WHERE t.status = 'AVAILABLE' ORDER BY t.createdAt ASC")
    List<Tray> findAvailableTrays();
}
