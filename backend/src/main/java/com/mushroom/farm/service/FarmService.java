package com.mushroom.farm.service;

import com.mushroom.common.dto.PageResponse;
import com.mushroom.farm.dto.CreateRoomRequest;
import com.mushroom.farm.dto.CreateTrayRequest;
import com.mushroom.farm.dto.RoomResponse;
import com.mushroom.farm.dto.TrayResponse;
import com.mushroom.farm.dto.UpdateRoomRequest;
import com.mushroom.farm.dto.UpdateTrayRequest;
import com.mushroom.farm.entity.GrowRoomStatus;
import com.mushroom.farm.entity.TrayStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface FarmService {

    // --- Quản lý phòng trồng (UC-FARM-001) ---
    PageResponse<RoomResponse> getRooms(GrowRoomStatus status, String search, Pageable pageable);

    RoomResponse createRoom(CreateRoomRequest request);

    RoomResponse getRoomById(UUID id);

    RoomResponse updateRoom(UUID id, UpdateRoomRequest request);

    void archiveRoom(UUID id);

    // --- Quản lý khay trồng (UC-FARM-002) ---
    PageResponse<TrayResponse> getTrays(UUID roomId, TrayStatus status, String search, Pageable pageable);

    TrayResponse createTray(CreateTrayRequest request);

    TrayResponse getTrayById(UUID id);

    TrayResponse updateTray(UUID id, UpdateTrayRequest request);

    void archiveTray(UUID id);

    List<TrayResponse> getAvailableTrays();
}
