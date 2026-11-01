package com.mushroom.farm.dto;

import com.mushroom.farm.entity.GrowRoom;
import com.mushroom.farm.entity.GrowRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private GrowRoomStatus status;
    private long totalTrays;
    private long activeTrays;
    private Instant createdAt;

    public static RoomResponse fromEntity(GrowRoom room, long totalTrays, long activeTrays) {
        return RoomResponse.builder()
                .id(room.getId())
                .code(room.getCode())
                .name(room.getName())
                .description(room.getDescription())
                .status(room.getStatus())
                .totalTrays(totalTrays)
                .activeTrays(activeTrays)
                .createdAt(room.getCreatedAt())
                .build();
    }
}
