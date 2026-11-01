package com.mushroom.rental.controller;

import com.mushroom.common.dto.ApiResponse;
import com.mushroom.common.dto.PageResponse;
import com.mushroom.rental.dto.LeaseResponse;
import com.mushroom.rental.dto.LinkTrayRequest;
import com.mushroom.rental.dto.MyTrayResponse;
import com.mushroom.rental.entity.LeaseStatus;
import com.mushroom.rental.service.RentalService;
import com.mushroom.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("isAuthenticated()")
public class MyTrayController {

    private final RentalService rentalService;

    public MyTrayController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping("/trays/link")
    public ResponseEntity<ApiResponse<MyTrayResponse>> linkTray(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody LinkTrayRequest request
    ) {
        MyTrayResponse response = rentalService.linkTrayByCode(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Liên kết khay trồng thành công", response));
    }

    @GetMapping("/my-trays")
    public ResponseEntity<ApiResponse<List<MyTrayResponse>>> getMyActiveTrays(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<MyTrayResponse> result = rentalService.getMyActiveTrays(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/my-trays/{trayId}")
    public ResponseEntity<ApiResponse<MyTrayResponse>> getMyTrayDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID trayId
    ) {
        MyTrayResponse response = rentalService.getMyTrayDetail(userPrincipal.getId(), trayId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-leases")
    public ResponseEntity<ApiResponse<PageResponse<LeaseResponse>>> getMyLeaseHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) LeaseStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<LeaseResponse> result = rentalService.getLeases(userPrincipal.getId(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
