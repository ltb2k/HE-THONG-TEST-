package com.mushroom.rental.dto;

import com.mushroom.rental.entity.LeaseTray;
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
public class LeaseTrayDetailResponse {

    private UUID id;
    private UUID trayId;
    private String uniqueCode;
    private String roomName;
    private String linkCode;
    private Instant linkCodeExpiresAt;
    private boolean isLinked;
    private Instant linkedAt;

    public static LeaseTrayDetailResponse fromEntity(LeaseTray lt) {
        return LeaseTrayDetailResponse.builder()
                .id(lt.getId())
                .trayId(lt.getTrayId())
                .uniqueCode(lt.getTray() != null ? lt.getTray().getUniqueCode() : null)
                .roomName(lt.getTray() != null && lt.getTray().getRoom() != null ? lt.getTray().getRoom().getName() : null)
                .linkCode(lt.getLinkCode())
                .linkCodeExpiresAt(lt.getLinkCodeExpiresAt())
                .isLinked(lt.getLinkedAt() != null)
                .linkedAt(lt.getLinkedAt())
                .build();
    }
}
