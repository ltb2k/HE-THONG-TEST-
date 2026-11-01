package com.mushroom.farm.service;

import com.mushroom.common.constant.ErrorCode;
import com.mushroom.common.dto.PageResponse;
import com.mushroom.common.exception.AppException;
import com.mushroom.farm.dto.CreateRoomRequest;
import com.mushroom.farm.dto.CreateTrayRequest;
import com.mushroom.farm.dto.RoomResponse;
import com.mushroom.farm.dto.TrayResponse;
import com.mushroom.farm.dto.UpdateRoomRequest;
import com.mushroom.farm.dto.UpdateTrayRequest;
import com.mushroom.farm.entity.GrowRoom;
import com.mushroom.farm.entity.GrowRoomStatus;
import com.mushroom.farm.entity.Tray;
import com.mushroom.farm.entity.TrayStatus;
import com.mushroom.farm.repository.GrowRoomRepository;
import com.mushroom.farm.repository.TrayRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class FarmServiceImpl implements FarmService {

    private final GrowRoomRepository roomRepository;
    private final TrayRepository trayRepository;

    public FarmServiceImpl(GrowRoomRepository roomRepository, TrayRepository trayRepository) {
        this.roomRepository = roomRepository;
        this.trayRepository = trayRepository;
    }

    // =========================================================================
    // Quản lý phòng trồng (UC-FARM-001)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoomResponse> getRooms(GrowRoomStatus status, String search, Pageable pageable) {
        Page<GrowRoom> roomPage = roomRepository.findByFilters(status, search, pageable);
        return PageResponse.of(roomPage.map(this::mapToRoomResponse));
    }

    @Override
    public RoomResponse createRoom(CreateRoomRequest request) {
        if (roomRepository.existsByCodeIgnoreCase(request.getCode().trim())) {
            throw new AppException(ErrorCode.FARM_ROOM_CONFLICT, "Mã phòng trồng đã tồn tại trong hệ thống.");
        }

        GrowRoom room = GrowRoom.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .description(request.getDescription())
                .status(GrowRoomStatus.ACTIVE)
                .build();

        return mapToRoomResponse(roomRepository.save(room));
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(UUID id) {
        GrowRoom room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FARM_ROOM_NOT_FOUND));
        return mapToRoomResponse(room);
    }

    @Override
    public RoomResponse updateRoom(UUID id, UpdateRoomRequest request) {
        GrowRoom room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FARM_ROOM_NOT_FOUND));

        if (request.getName() != null && !request.getName().isBlank()) {
            room.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            room.setDescription(request.getDescription().trim());
        }
        if (request.getStatus() != null) {
            if (request.getStatus() == GrowRoomStatus.ARCHIVED) {
                // Kiểm tra ràng buộc tài nguyên liên quan (BR-FARM-001)
                validateRoomCanBeArchived(room.getId());
            }
            room.setStatus(request.getStatus());
        }

        return mapToRoomResponse(roomRepository.save(room));
    }

    @Override
    public void archiveRoom(UUID id) {
        GrowRoom room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FARM_ROOM_NOT_FOUND));

        // Ràng buộc BR-FARM-001: không xóa cứng; kiểm tra khay đang hoạt động
        validateRoomCanBeArchived(room.getId());

        room.setStatus(GrowRoomStatus.ARCHIVED);
        roomRepository.save(room);
    }

    private void validateRoomCanBeArchived(UUID roomId) {
        long activeTrayCount = trayRepository.countByRoomIdAndStatusIn(
                roomId,
                Set.of(TrayStatus.ASSIGNED, TrayStatus.IN_USE)
        );
        if (activeTrayCount > 0) {
            throw new AppException(ErrorCode.FARM_ROOM_CONFLICT,
                    "Phòng đang có " + activeTrayCount + " khay đang được sử dụng hoặc phân công. Vui lòng chuyển hoặc giải phóng khay trước khi lưu trữ phòng.");
        }
    }

    private RoomResponse mapToRoomResponse(GrowRoom room) {
        long totalTrays = trayRepository.countByRoomId(room.getId());
        long activeTrays = trayRepository.countByRoomIdAndStatusIn(
                room.getId(),
                Set.of(TrayStatus.ASSIGNED, TrayStatus.IN_USE)
        );
        return RoomResponse.fromEntity(room, totalTrays, activeTrays);
    }

    // =========================================================================
    // Quản lý khay trồng (UC-FARM-002)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TrayResponse> getTrays(UUID roomId, TrayStatus status, String search, Pageable pageable) {
        Page<Tray> trayPage = trayRepository.findByFilters(roomId, status, search, pageable);
        return PageResponse.of(trayPage.map(TrayResponse::fromEntity));
    }

    @Override
    public TrayResponse createTray(CreateTrayRequest request) {
        // Kiểm tra tính duy nhất của mã khay toàn hệ thống (BR-RENT-001)
        if (trayRepository.existsByUniqueCodeIgnoreCase(request.getUniqueCode().trim())) {
            throw new AppException(ErrorCode.TRAY_CONFLICT, "Mã khay duy nhất đã tồn tại trong hệ thống.");
        }

        // Kiểm tra phòng đích tồn tại và đang hoạt động
        GrowRoom room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.FARM_ROOM_NOT_FOUND));

        if (room.getStatus() == GrowRoomStatus.ARCHIVED) {
            throw new AppException(ErrorCode.FARM_ROOM_CONFLICT, "Không thể thêm khay vào phòng đã lưu trữ/ngừng hoạt động.");
        }

        Tray tray = Tray.builder()
                .uniqueCode(request.getUniqueCode().trim().toUpperCase())
                .roomId(room.getId())
                .room(room)
                .status(TrayStatus.AVAILABLE)
                .description(request.getDescription())
                .build();

        return TrayResponse.fromEntity(trayRepository.save(tray));
    }

    @Override
    @Transactional(readOnly = true)
    public TrayResponse getTrayById(UUID id) {
        Tray tray = trayRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAY_NOT_FOUND));
        return TrayResponse.fromEntity(tray);
    }

    @Override
    public TrayResponse updateTray(UUID id, UpdateTrayRequest request) {
        Tray tray = trayRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAY_NOT_FOUND));

        if (request.getRoomId() != null && !request.getRoomId().equals(tray.getRoomId())) {
            GrowRoom newRoom = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new AppException(ErrorCode.FARM_ROOM_NOT_FOUND));
            if (newRoom.getStatus() == GrowRoomStatus.ARCHIVED) {
                throw new AppException(ErrorCode.FARM_ROOM_CONFLICT, "Không thể chuyển khay sang phòng đã lưu trữ.");
            }
            tray.setRoomId(newRoom.getId());
            tray.setRoom(newRoom);
        }

        if (request.getStatus() != null) {
            if (request.getStatus() == TrayStatus.ARCHIVED) {
                // Kiểm tra ràng buộc BR-FARM-001, BR-RENT-001
                if (tray.getStatus() == TrayStatus.ASSIGNED || tray.getStatus() == TrayStatus.IN_USE) {
                    throw new AppException(ErrorCode.TRAY_CONFLICT, "Khay đang được liên kết hoặc đang trong hợp đồng thuê, không thể chuyển sang lưu trữ.");
                }
            }
            tray.setStatus(request.getStatus());
        }

        if (request.getDescription() != null) {
            tray.setDescription(request.getDescription().trim());
        }

        return TrayResponse.fromEntity(trayRepository.save(tray));
    }

    @Override
    public void archiveTray(UUID id) {
        Tray tray = trayRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TRAY_NOT_FOUND));

        // Ràng buộc BR-FARM-001 & BR-RENT-001: không xóa cứng khay đang có lease
        if (tray.getStatus() == TrayStatus.ASSIGNED || tray.getStatus() == TrayStatus.IN_USE) {
            throw new AppException(ErrorCode.TRAY_CONFLICT, "Khay đang được liên kết hoặc đang nuôi trồng, không thể lưu trữ/xóa.");
        }

        tray.setStatus(TrayStatus.ARCHIVED);
        trayRepository.save(tray);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrayResponse> getAvailableTrays() {
        return trayRepository.findAvailableTrays().stream()
                .map(TrayResponse::fromEntity)
                .toList();
    }
}
