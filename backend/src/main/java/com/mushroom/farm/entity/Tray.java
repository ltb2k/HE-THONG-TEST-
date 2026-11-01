package com.mushroom.farm.entity;

import com.mushroom.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "trays")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tray extends BaseEntity {

    @Column(name = "unique_code", nullable = false, unique = true, length = 50)
    private String uniqueCode;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    private GrowRoom room;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TrayStatus status = TrayStatus.AVAILABLE;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
