package com.mushroom.rental.dto;

import com.mushroom.rental.entity.PackageStatus;
import com.mushroom.rental.entity.RentalPackage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageResponse {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal priceVnd;
    private int durationDays;
    private PackageStatus status;
    private Instant createdAt;

    public static PackageResponse fromEntity(RentalPackage pkg) {
        return PackageResponse.builder()
                .id(pkg.getId())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .priceVnd(pkg.getPriceVnd())
                .durationDays(pkg.getDurationDays())
                .status(pkg.getStatus())
                .createdAt(pkg.getCreatedAt())
                .build();
    }
}
