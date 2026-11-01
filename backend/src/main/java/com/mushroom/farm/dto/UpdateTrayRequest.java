package com.mushroom.farm.dto;

import com.mushroom.farm.entity.TrayStatus;
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
public class UpdateTrayRequest {

    private UUID roomId;

    private TrayStatus status;

    private String description;
}
