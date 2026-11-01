package com.mushroom.rental.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeaseRequest {

    @NotNull(message = "Gói thuê không được để trống")
    private UUID packageId;

    @NotNull(message = "Số lượng khay không được để trống")
    @Min(value = 1, message = "Số lượng khay thuê tối thiểu là 1")
    @Max(value = 5, message = "Số lượng khay thuê tối đa là 5 theo quy định")
    private Integer quantity;
}
