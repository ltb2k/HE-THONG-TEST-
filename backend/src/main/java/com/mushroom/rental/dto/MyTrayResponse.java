package com.mushroom.rental.dto;

import com.mushroom.farm.entity.TrayStatus;
import com.mushroom.rental.entity.LeaseTray;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyTrayResponse {

    private UUID trayId;
    private String uniqueCode;
    private UUID roomId;
    private String roomName;
    private TrayStatus status;
    private UUID leaseId;
    private String packageName;
    private Instant startDate;
    private Instant endDate;
    private long daysRemaining;

    public static MyTrayResponse fromEntity(LeaseTray lt) {
        long daysLeft = 0;
        if (lt.getLease() != null && lt.getLease().getEndDate() != null) {
            Duration duration = Duration.between(Instant.now(), lt.getLease().getEndDate());
            daysLeft = Math.max(0, duration.toDays());
        }

        return MyTrayResponse.builder()
                .trayId(lt.getTrayId())
                .uniqueCode(lt.getTray() != null ? lt.getTray().getUniqueCode() : null)
                .roomId(lt.getTray() != null ? lt.getTray().getRoomId() : null)
                .roomName(lt.getTray() != null && lt.getTray().getRoom() != null ? lt.getTray().getRoom().getName() : null)
                .status(lt.getTray() != null ? lt.getTray().getStatus() : null)
                .leaseId(lt.getLeaseId())
                .packageName(lt.getLease() != null && lt.getLease().getRentalPackage() != null
                        ? lt.getLease().getRentalPackage().getName() : null)
                .startDate(lt.getLease() != null ? lt.getLease().getStartDate() : null)
                .endDate(lt.getLease() != null ? lt.getLease().getEndDate() : null)
                .daysRemaining(daysLeft)
                .build();
    }
}
