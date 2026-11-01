package com.mushroom.rental.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreatePackageRequest {

    @NotBlank(message = "Tên gói không được để trống")
    @Size(max = 100, message = "Tên gói không vượt quá 100 ký tự")
    private String name;

    private String description;

    @NotNull(message = "Giá gói không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá gói phải lớn hơn 0")
    private BigDecimal priceVnd;

    @NotNull(message = "Thời hạn thuê (ngày) không được để trống")
    @Min(value = 1, message = "Thời hạn thuê tối thiểu 1 ngày")
    private Integer durationDays;
}
