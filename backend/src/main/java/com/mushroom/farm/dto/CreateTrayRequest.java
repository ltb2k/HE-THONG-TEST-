package com.mushroom.farm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateTrayRequest {

    @NotBlank(message = "Mã khay duy nhất không được để trống")
    @Size(max = 50, message = "Mã khay không vượt quá 50 ký tự")
    private String uniqueCode;

    @NotNull(message = "Phòng trồng không được để trống")
    private UUID roomId;

    private String description;
}
