package com.mushroom.rental.dto;

import com.mushroom.rental.entity.Lease;
import com.mushroom.rental.entity.LeaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaseResponse {

    private UUID id;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private UUID packageId;
    private String packageName;
    private BigDecimal priceVnd;
    private int quantity;
    private LeaseStatus status;
    private String cancelReason;
    private Instant startDate;
    private Instant endDate;
    private List<LeaseTrayDetailResponse> trays;
    private Instant createdAt;

    public static LeaseResponse fromEntity(Lease lease) {
        List<LeaseTrayDetailResponse> trayDtos = lease.getLeaseTrays() != null
                ? lease.getLeaseTrays().stream().map(LeaseTrayDetailResponse::fromEntity).toList()
                : Collections.emptyList();

        return LeaseResponse.builder()
                .id(lease.getId())
                .customerId(lease.getCustomerId())
                .customerName(lease.getCustomer() != null ? lease.getCustomer().getFullName() : null)
                .customerEmail(lease.getCustomer() != null ? lease.getCustomer().getEmail() : null)
                .packageId(lease.getPackageId())
                .packageName(lease.getRentalPackage() != null ? lease.getRentalPackage().getName() : null)
                .priceVnd(lease.getRentalPackage() != null ? lease.getRentalPackage().getPriceVnd() : null)
                .quantity(lease.getQuantity())
                .status(lease.getStatus())
                .cancelReason(lease.getCancelReason())
                .startDate(lease.getStartDate())
                .endDate(lease.getEndDate())
                .trays(trayDtos)
                .createdAt(lease.getCreatedAt())
                .build();
    }
}
