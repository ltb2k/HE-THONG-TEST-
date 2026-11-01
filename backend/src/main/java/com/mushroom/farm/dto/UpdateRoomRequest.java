package com.mushroom.farm.dto;

import com.mushroom.farm.entity.GrowRoomStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoomRequest {

    @Size(max = 100, message = "Tên phòng không vượt quá 100 ký tự")
    private String name;

    private String description;

    private GrowRoomStatus status;
}
