package com.mushroom.rental.service;

import com.mushroom.common.dto.PageResponse;
import com.mushroom.rental.dto.AssignTraysRequest;
import com.mushroom.rental.dto.CancelLeaseRequest;
import com.mushroom.rental.dto.CreateLeaseRequest;
import com.mushroom.rental.dto.CreatePackageRequest;
import com.mushroom.rental.dto.LeaseResponse;
import com.mushroom.rental.dto.LinkTrayRequest;
import com.mushroom.rental.dto.MyTrayResponse;
import com.mushroom.rental.dto.PackageResponse;
import com.mushroom.rental.dto.RejectLeaseRequest;
import com.mushroom.rental.dto.UpdatePackageRequest;
import com.mushroom.rental.entity.LeaseStatus;
import com.mushroom.rental.entity.PackageStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface RentalService {

    // --- Quản lý gói thuê (UC-RENT-001) ---
    List<PackageResponse> getActivePackages();

    PageResponse<PackageResponse> getAllPackages(PackageStatus status, String search, Pageable pageable);

    PackageResponse createPackage(CreatePackageRequest request);

    PackageResponse updatePackage(UUID id, UpdatePackageRequest request);

    // --- Đăng ký & Xử lý đơn thuê (UC-RENT-002, UC-RENT-003, UC-RENT-006) ---
    LeaseResponse createLeaseOrder(UUID customerId, CreateLeaseRequest request);

    PageResponse<LeaseResponse> getLeases(UUID customerId, LeaseStatus status, Pageable pageable);

    LeaseResponse getLeaseById(UUID leaseId, UUID currentUserId, boolean isAdminOrOperator);

    LeaseResponse approveLease(UUID leaseId);

    LeaseResponse rejectLease(UUID leaseId, RejectLeaseRequest request);

    LeaseResponse assignTrays(UUID leaseId, AssignTraysRequest request);

    void cancelLease(UUID leaseId, UUID currentUserId, boolean isAdminOrOperator, CancelLeaseRequest request);

    // --- Liên kết khay & Theo dõi khay thuê (UC-RENT-004, UC-RENT-005) ---
    MyTrayResponse linkTrayByCode(UUID customerId, LinkTrayRequest request);

    List<MyTrayResponse> getMyActiveTrays(UUID customerId);

    MyTrayResponse getMyTrayDetail(UUID customerId, UUID trayId);
}
