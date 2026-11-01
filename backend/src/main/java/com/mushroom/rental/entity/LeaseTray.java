package com.mushroom.rental.entity;

import com.mushroom.farm.entity.Tray;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lease_trays")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaseTray {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "lease_id", nullable = false)
    private UUID leaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lease_id", insertable = false, updatable = false)
    private Lease lease;

    @Column(name = "tray_id", nullable = false)
    private UUID trayId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tray_id", insertable = false, updatable = false)
    private Tray tray;

    @Column(name = "assigned_at", nullable = false)
    @Builder.Default
    private Instant assignedAt = Instant.now();

    @Column(name = "link_code", unique = true, length = 50)
    private String linkCode;

    @Column(name = "link_code_expires_at")
    private Instant linkCodeExpiresAt;

    @Column(name = "link_code_used_at")
    private Instant linkCodeUsedAt;

    @Column(name = "linked_at")
    private Instant linkedAt;

    public boolean isCodeExpired() {
        return linkCodeExpiresAt != null && linkCodeExpiresAt.isBefore(Instant.now());
    }

    public boolean isCodeUsed() {
        return linkCodeUsedAt != null;
    }
}
