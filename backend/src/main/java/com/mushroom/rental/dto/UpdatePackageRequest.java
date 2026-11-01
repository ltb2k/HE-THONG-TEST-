package com.mushroom.rental.dto;

import com.mushroom.rental.entity.PackageStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePackageRequest {

    @Size(max = 100, message = "Tên gói không vượt quá 100 ký tự")
    private String name;

    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá gói phải lớn hơn 0")
    private BigDecimal priceVnd;

    @Min(value = 1, message = "Thời hạn thuê tối thiểu 1 ngày")
    private Integer durationDays;

    private PackageStatus status;
}
