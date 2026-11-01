package com.mushroom.rental.controller;

import com.mushroom.common.dto.ApiResponse;
import com.mushroom.common.dto.PageResponse;
import com.mushroom.rental.dto.AssignTraysRequest;
import com.mushroom.rental.dto.CancelLeaseRequest;
import com.mushroom.rental.dto.CreateLeaseRequest;
import com.mushroom.rental.dto.LeaseResponse;
import com.mushroom.rental.dto.RejectLeaseRequest;
import com.mushroom.rental.entity.LeaseStatus;
import com.mushroom.rental.service.RentalService;
import com.mushroom.security.UserPrincipal;
import com.mushroom.user.entity.Role;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leases")
@PreAuthorize("isAuthenticated()")
public class LeaseController {

    private final RentalService rentalService;

    public LeaseController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LeaseResponse>> createLeaseOrder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateLeaseRequest request
    ) {
        LeaseResponse response = rentalService.createLeaseOrder(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LeaseResponse>>> getLeases(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) LeaseStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        boolean isStaff = userPrincipal.getRole() == Role.ADMIN || userPrincipal.getRole() == Role.OPERATOR;
        UUID customerFilter = isStaff ? null : userPrincipal.getId();

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<LeaseResponse> result = rentalService.getLeases(customerFilter, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaseResponse>> getLeaseById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id
    ) {
        boolean isStaff = userPrincipal.getRole() == Role.ADMIN || userPrincipal.getRole() == Role.OPERATOR;
        LeaseResponse response = rentalService.getLeaseById(id, userPrincipal.getId(), isStaff);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<LeaseResponse>> approveLease(@PathVariable UUID id) {
        LeaseResponse response = rentalService.approveLease(id);
        return ResponseEntity.ok(ApiResponse.success("Duyệt đơn thuê thành công", response));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<LeaseResponse>> rejectLease(
            @PathVariable UUID id,
            @Valid @RequestBody RejectLeaseRequest request
    ) {
        LeaseResponse response = rentalService.rejectLease(id, request);
        return ResponseEntity.ok(ApiResponse.success("Từ chối đơn thuê thành công", response));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<LeaseResponse>> assignTrays(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTraysRequest request
    ) {
        LeaseResponse response = rentalService.assignTrays(id, request);
        return ResponseEntity.ok(ApiResponse.success("Phân công khay và phát mã liên kết thành công", response));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelLease(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id,
            @RequestBody(required = false) CancelLeaseRequest request
    ) {
        boolean isStaff = userPrincipal.getRole() == Role.ADMIN || userPrincipal.getRole() == Role.OPERATOR;
        CancelLeaseRequest cancelReq = request != null ? request : new CancelLeaseRequest();
        rentalService.cancelLease(id, userPrincipal.getId(), isStaff, cancelReq);
        return ResponseEntity.ok(ApiResponse.success("Hủy đơn thuê thành công", null));
    }
}
