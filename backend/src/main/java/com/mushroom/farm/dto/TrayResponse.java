package com.mushroom.farm.dto;

import com.mushroom.farm.entity.Tray;
import com.mushroom.farm.entity.TrayStatus;
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
public class TrayResponse {

    private UUID id;
    private String uniqueCode;
    private UUID roomId;
    private String roomName;
    private TrayStatus status;
    private String description;
    private Instant createdAt;

    public static TrayResponse fromEntity(Tray tray) {
        return TrayResponse.builder()
                .id(tray.getId())
                .uniqueCode(tray.getUniqueCode())
                .roomId(tray.getRoomId())
                .roomName(tray.getRoom() != null ? tray.getRoom().getName() : null)
                .status(tray.getStatus())
                .description(tray.getDescription())
                .createdAt(tray.getCreatedAt())
                .build();
    }
}
