package com.mushroom.rental.controller;

import com.mushroom.common.dto.ApiResponse;
import com.mushroom.common.dto.PageResponse;
import com.mushroom.rental.dto.CreatePackageRequest;
import com.mushroom.rental.dto.PackageResponse;
import com.mushroom.rental.dto.UpdatePackageRequest;
import com.mushroom.rental.entity.PackageStatus;
import com.mushroom.rental.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/rental-packages")
public class RentalPackageController {

    private final RentalService rentalService;

    public RentalPackageController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getActivePackages() {
        List<PackageResponse> result = rentalService.getActivePackages();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<PackageResponse>>> getAllPackages(
            @RequestParam(required = false) PackageStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Direction.ASC, "priceVnd"));
        PageResponse<PackageResponse> result = rentalService.getAllPackages(status, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PackageResponse>> createPackage(@Valid @RequestBody CreatePackageRequest request) {
        PackageResponse response = rentalService.createPackage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PackageResponse>> updatePackage(@PathVariable UUID id,
                                                                      @Valid @RequestBody UpdatePackageRequest request) {
        PackageResponse response = rentalService.updatePackage(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật gói thuê thành công", response));
    }
}
