package com.mushroom.farm.controller;

import com.mushroom.common.dto.ApiResponse;
import com.mushroom.common.dto.PageResponse;
import com.mushroom.farm.dto.CreateTrayRequest;
import com.mushroom.farm.dto.TrayResponse;
import com.mushroom.farm.dto.UpdateTrayRequest;
import com.mushroom.farm.entity.TrayStatus;
import com.mushroom.farm.service.FarmService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trays")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
public class TrayController {

    private final FarmService farmService;

    public TrayController(FarmService farmService) {
        this.farmService = farmService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TrayResponse>>> getTrays(
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) TrayStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Direction.ASC, "uniqueCode"));
        PageResponse<TrayResponse> result = farmService.getTrays(roomId, status, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<TrayResponse>>> getAvailableTrays() {
        List<TrayResponse> result = farmService.getAvailableTrays();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TrayResponse>> createTray(@Valid @RequestBody CreateTrayRequest request) {
        TrayResponse response = farmService.createTray(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrayResponse>> getTrayById(@PathVariable UUID id) {
        TrayResponse response = farmService.getTrayById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TrayResponse>> updateTray(@PathVariable UUID id,
                                                                @Valid @RequestBody UpdateTrayRequest request) {
        TrayResponse response = farmService.updateTray(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật khay trồng thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> archiveTray(@PathVariable UUID id) {
        farmService.archiveTray(id);
        return ResponseEntity.ok(ApiResponse.success("Lưu trữ khay trồng thành công", null));
    }
}
