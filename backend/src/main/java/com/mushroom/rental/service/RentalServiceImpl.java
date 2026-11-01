package com.mushroom.rental.service;

import com.mushroom.common.constant.ErrorCode;
import com.mushroom.common.dto.PageResponse;
import com.mushroom.common.exception.AppException;
import com.mushroom.farm.entity.Tray;
import com.mushroom.farm.entity.TrayStatus;
import com.mushroom.farm.repository.TrayRepository;
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
import com.mushroom.rental.entity.Lease;
import com.mushroom.rental.entity.LeaseStatus;
import com.mushroom.rental.entity.LeaseTray;
import com.mushroom.rental.entity.PackageStatus;
import com.mushroom.rental.entity.RentalPackage;
import com.mushroom.rental.entity.Transaction;
import com.mushroom.rental.entity.TransactionStatus;
import com.mushroom.rental.repository.LeaseRepository;
import com.mushroom.rental.repository.LeaseTrayRepository;
import com.mushroom.rental.repository.RentalPackageRepository;
import com.mushroom.rental.repository.TransactionRepository;
import com.mushroom.user.entity.User;
import com.mushroom.user.entity.UserStatus;
import com.mushroom.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RentalServiceImpl implements RentalService {

    private final RentalPackageRepository packageRepository;
    private final LeaseRepository leaseRepository;
    private final LeaseTrayRepository leaseTrayRepository;
    private final TransactionRepository transactionRepository;
    private final TrayRepository trayRepository;
    private final UserRepository userRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public RentalServiceImpl(RentalPackageRepository packageRepository,
                             LeaseRepository leaseRepository,
                             LeaseTrayRepository leaseTrayRepository,
                             TransactionRepository transactionRepository,
                             TrayRepository trayRepository,
                             UserRepository userRepository) {
        this.packageRepository = packageRepository;
        this.leaseRepository = leaseRepository;
        this.leaseTrayRepository = leaseTrayRepository;
        this.transactionRepository = transactionRepository;
        this.trayRepository = trayRepository;
        this.userRepository = userRepository;
    }

    // =========================================================================
    // Quản lý gói thuê (UC-RENT-001)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponse> getActivePackages() {
        return packageRepository.findByStatus(PackageStatus.ACTIVE).stream()
                .map(PackageResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PackageResponse> getAllPackages(PackageStatus status, String search, Pageable pageable) {
        Page<RentalPackage> page = packageRepository.findByFilters(status, search, pageable);
        return PageResponse.of(page.map(PackageResponse::fromEntity));
    }

    @Override
    public PackageResponse createPackage(CreatePackageRequest request) {
        RentalPackage pkg = RentalPackage.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .priceVnd(request.getPriceVnd())
                .durationDays(request.getDurationDays())
                .status(PackageStatus.ACTIVE)
                .build();

        return PackageResponse.fromEntity(packageRepository.save(pkg));
    }

    @Override
    public PackageResponse updatePackage(UUID id, UpdatePackageRequest request) {
        RentalPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEASE_PACKAGE_NOT_FOUND));

        if (request.getName() != null && !request.getName().isBlank()) {
            pkg.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            pkg.setDescription(request.getDescription().trim());
        }
        if (request.getPriceVnd() != null) {
            pkg.setPriceVnd(request.getPriceVnd());
        }
        if (request.getDurationDays() != null) {
            pkg.setDurationDays(request.getDurationDays());
        }
        if (request.getStatus() != null) {
            pkg.setStatus(request.getStatus());
        }

        return PackageResponse.fromEntity(packageRepository.save(pkg));
    }

    // =========================================================================
    // Đăng ký & Xử lý đơn thuê (UC-RENT-002, UC-RENT-003, UC-RENT-006)
    // =========================================================================

    @Override
    public LeaseResponse createLeaseOrder(UUID customerId, CreateLeaseRequest request) {
        // 1. Kiểm tra tài khoản khách hàng hợp lệ và đã xác minh email (BR-SEC-001, UC-RENT-002)
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (customer.getStatus() != UserStatus.ACTIVE || customer.getEmailVerifiedAt() == null) {
            throw new AppException(ErrorCode.AUTH_FORBIDDEN, "Tài khoản cần xác minh email trước khi đăng ký thuê khay.");
        }

        // 2. Kiểm tra gói thuê có đang ACTIVE không (BR-RENT-002)
        RentalPackage pkg = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new AppException(ErrorCode.LEASE_PACKAGE_NOT_FOUND));

        if (pkg.getStatus() != PackageStatus.ACTIVE) {
            throw new AppException(ErrorCode.LEASE_PACKAGE_NOT_FOUND, "Gói thuê hiện không còn cung cấp.");
        }

        // 3. Kiểm tra số lượng khay khả dụng của trang trại (BR-RENT-002)
        List<Tray> availableTrays = trayRepository.findAvailableTrays();
        if (availableTrays.size() < request.getQuantity()) {
            throw new AppException(ErrorCode.LEASE_RESOURCE_UNAVAILABLE,
                    "Không đủ khay khả dụng trong hệ thống. Hiện chỉ còn " + availableTrays.size() + " khay sẵn sàng.");
        }

        // 4. Tạo yêu cầu thuê PENDING
        Lease lease = Lease.builder()
                .customerId(customer.getId())
                .customer(customer)
                .packageId(pkg.getId())
                .rentalPackage(pkg)
                .quantity(request.getQuantity())
                .status(LeaseStatus.PENDING)
                .build();
        lease = leaseRepository.save(lease);

        // 5. Tạo giao dịch mô phỏng (SIMULATED - BR-RENT-002: không thu tiền thật trong MVP)
        BigDecimal totalAmount = pkg.getPriceVnd().multiply(BigDecimal.valueOf(request.getQuantity()));
        Transaction transaction = Transaction.builder()
                .leaseId(lease.getId())
                .amount(totalAmount)
                .status(TransactionStatus.SIMULATED)
                .build();
        transactionRepository.save(transaction);

        return LeaseResponse.fromEntity(lease);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeaseResponse> getLeases(UUID customerId, LeaseStatus status, Pageable pageable) {
        Page<Lease> page = leaseRepository.findByFilters(customerId, status, pageable);
        return PageResponse.of(page.map(LeaseResponse::fromEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public LeaseResponse getLeaseById(UUID leaseId, UUID currentUserId, boolean isAdminOrOperator) {
        Lease lease = leaseRepository.findByIdWithDetails(leaseId)
                .orElseThrow(() -> new AppException(ErrorCode.LEASE_PACKAGE_NOT_FOUND, "Không tìm thấy đơn thuê."));

        // Bảo vệ phân quyền dữ liệu khách hàng (BR-RENT-003)
        if (!isAdminOrOperator && !lease.getCustomerId().equals(currentUserId)) {
            throw new AppException(ErrorCode.AUTH_FORBIDDEN, "Bạn không có quyền truy cập đơn thuê này.");
        }

        return LeaseResponse.fromEntity(lease);
    }

    @Override
    public LeaseResponse approveLease(UUID leaseId) {
        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new AppException(ErrorCode.LEASE_PACKAGE_NOT_FOUND, "Không tìm thấy đơn thuê."));

        if (lease.getStatus() != LeaseStatus.PENDING) {
            throw new AppException(ErrorCode.LEASE_RESOURCE_UNAVAILABLE, "Đơn thuê không ở trạng thái PENDING để duyệt.");
        }

        lease.setStatus(LeaseStatus.APPROVED);
        return LeaseResponse.fromEntity(leaseRepository.save(lease));
    }

    @Override
    public LeaseResponse rejectLease(UUID leaseId, RejectLeaseRequest request) {
        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new AppException(ErrorCode.LEASE_PACKAGE_NOT_FOUND, "Không tìm thấy đơn thuê."));

        if (lease.getStatus() != LeaseStatus.PENDING && lease.getStatus() != LeaseStatus.APPROVED) {
            throw new AppException(ErrorCode.LEASE_RESOURCE_UNAVAILABLE, "Không thể từ chối đơn ở trạng thái hiện tại.");
        }

        lease.setStatus(LeaseStatus.REJECTED);
        lease.setCancelReason(request.getReason().trim());
        lease = leaseRepository.save(lease);

        // Cập nhật giao dịch mô phỏng -> SIMULATED_REJECTED
        transactionRepository.findTopByLeaseIdOrderByCreatedAtDesc(lease.getId())
                .ifPresent(tx -> {
                    tx.setStatus(TransactionStatus.SIMULATED_REJECTED);
                    transactionRepository.save(tx);
                });

        return LeaseResponse.fromEntity(lease);
    }

    @Override
    public LeaseResponse assignTrays(UUID leaseId, AssignTraysRequest request) {
        Lease lease = leaseRepository.findByIdWithDetails(leaseId)
                .orElseThrow(() -> new AppException(ErrorCode.LEASE_PACKAGE_NOT_FOUND, "Không tìm thấy đơn thuê."));

        if (lease.getStatus() != LeaseStatus.APPROVED && lease.getStatus() != LeaseStatus.PENDING) {
            throw new AppException(ErrorCode.LEASE_RESOURCE_UNAVAILABLE, "Đơn thuê không ở trạng thái có thể phân công khay.");
        }

        if (request.getTrayIds().size() != lease.getQuantity()) {
            throw new AppException(ErrorCode.COMMON_VALIDATION_FAILED,
                    "Số lượng khay phân công (" + request.getTrayIds().size() + ") không khớp với số lượng đơn đặt (" + lease.getQuantity() + ").");
        }

        List<LeaseTray> newLeaseTrays = new ArrayList<>();

        for (UUID trayId : request.getTrayIds()) {
            Tray tray = trayRepository.findById(trayId)
                    .orElseThrow(() -> new AppException(ErrorCode.TRAY_NOT_FOUND, "Không tìm thấy khay: " + trayId));

            if (tray.getStatus() != TrayStatus.AVAILABLE) {
                throw new AppException(ErrorCode.TRAY_CONFLICT, "Khay " + tray.getUniqueCode() + " không ở trạng thái sẵn sàng (AVAILABLE).");
            }

            // Kiểm tra quy tắc BR-RENT-001: Mỗi khay chỉ có tối đa một lease ACTIVE
            if (leaseTrayRepository.existsByTrayIdAndLeaseStatus(tray.getId(), LeaseStatus.ACTIVE)) {
                throw new AppException(ErrorCode.TRAY_CONFLICT, "Khay " + tray.getUniqueCode() + " đang có hợp đồng ACTIVE khác.");
            }

            // Cập nhật trạng thái khay -> ASSIGNED
            tray.setStatus(TrayStatus.ASSIGNED);
            trayRepository.save(tray);

            // Cấp mã liên kết một lần, hết hạn sau 24 giờ (BR-RENT-001, UC-RENT-003)
            String linkCode = generateLinkCode();
            Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);

            LeaseTray leaseTray = LeaseTray.builder()
                    .leaseId(lease.getId())
                    .lease(lease)
                    .trayId(tray.getId())
                    .tray(tray)
                    .assignedAt(Instant.now())
                    .linkCode(linkCode)
                    .linkCodeExpiresAt(expiresAt)
                    .build();

            newLeaseTrays.add(leaseTray);
        }

        leaseTrayRepository.saveAll(newLeaseTrays);

        // Chuyển trạng thái đơn sang ASSIGNED
        lease.setStatus(LeaseStatus.ASSIGNED);
        lease.setLeaseTrays(newLeaseTrays);
        lease = leaseRepository.save(lease);

        // Cập nhật giao dịch mô phỏng -> SIMULATED_CONFIRMED (BR-RENT-002)
        transactionRepository.findTopByLeaseIdOrderByCreatedAtDesc(lease.getId())
                .ifPresent(tx -> {
                    tx.setStatus(TransactionStatus.SIMULATED_CONFIRMED);
                    transactionRepository.save(tx);
                });

        return LeaseResponse.fromEntity(lease);
    }

    @Override
    public void cancelLease(UUID leaseId, UUID currentUserId, boolean isAdminOrOperator, CancelLeaseRequest request) {
        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new AppException(ErrorCode.LEASE_PACKAGE_NOT_FOUND, "Không tìm thấy đơn thuê."));

        if (!isAdminOrOperator) {
            // Customer chỉ được hủy đơn TRƯỚC trạng thái ASSIGNED (BR-RENT-002, UC-RENT-006)
            if (!lease.getCustomerId().equals(currentUserId)) {
                throw new AppException(ErrorCode.AUTH_FORBIDDEN);
            }
            if (lease.getStatus() != LeaseStatus.PENDING && lease.getStatus() != LeaseStatus.APPROVED) {
                throw new AppException(ErrorCode.LEASE_RESOURCE_UNAVAILABLE,
                        "Khách hàng chỉ có thể hủy đơn trước khi phân công khay (ASSIGNED). Sau thời điểm này vui lòng liên hệ Operator.");
            }
        } else {
            // Operator/Admin hủy từ ASSIGNED trở đi bắt buộc phải có lý do (BR-RENT-002)
            if (lease.getStatus() == LeaseStatus.ASSIGNED || lease.getStatus() == LeaseStatus.ACTIVE) {
                if (request.getReason() == null || request.getReason().isBlank()) {
                    throw new AppException(ErrorCode.COMMON_VALIDATION_FAILED, "Bắt buộc nhập lý do khi hủy đơn đã phân công/kích hoạt.");
                }
            }
        }

        // Giải phóng khay đã gán về trạng thái AVAILABLE
        List<LeaseTray> leaseTrays = leaseTrayRepository.findByLeaseId(lease.getId());
        for (LeaseTray lt : leaseTrays) {
            trayRepository.findById(lt.getTrayId()).ifPresent(tray -> {
                if (tray.getStatus() == TrayStatus.ASSIGNED || tray.getStatus() == TrayStatus.IN_USE) {
                    tray.setStatus(TrayStatus.AVAILABLE);
                    trayRepository.save(tray);
                }
            });
        }

        lease.setStatus(LeaseStatus.CANCELLED);
        lease.setCancelReason(request.getReason() != null ? request.getReason().trim() : "Khách hàng hủy đơn");
        leaseRepository.save(lease);
    }

    // =========================================================================
    // Liên kết khay bằng mã (UC-RENT-004, UC-RENT-005)
    // =========================================================================

    @Override
    public MyTrayResponse linkTrayByCode(UUID customerId, LinkTrayRequest request) {
        String code = request.getLinkCode().trim().toUpperCase();

        LeaseTray leaseTray = leaseTrayRepository.findByLinkCodeWithDetails(code)
                .orElseThrow(() -> new AppException(ErrorCode.TRAY_NOT_FOUND, "Mã liên kết không tồn tại hoặc không hợp lệ."));

        Lease lease = leaseTray.getLease();

        // 1. Kiểm tra mã có thuộc đơn của khách hàng này không (BR-RENT-001)
        if (!lease.getCustomerId().equals(customerId)) {
            throw new AppException(ErrorCode.TRAY_CONFLICT, "Mã liên kết không thuộc tài khoản của bạn.");
        }

        // 2. Kiểm tra mã đã dùng chưa
        if (leaseTray.isCodeUsed()) {
            throw new AppException(ErrorCode.TRAY_CONFLICT, "Mã liên kết này đã được sử dụng trước đó.");
        }

        // 3. Kiểm tra mã có hết hạn 24 giờ không (BR-RENT-001)
        if (leaseTray.isCodeExpired()) {
            throw new AppException(ErrorCode.TRAY_CONFLICT, "Mã liên kết đã hết hạn (24 giờ). Vui lòng liên hệ Operator để cấp mã mới.");
        }

        // 4. Kiểm tra khay không có hợp đồng ACTIVE khác
        Tray tray = leaseTray.getTray();
        if (leaseTrayRepository.existsByTrayIdAndLeaseStatus(tray.getId(), LeaseStatus.ACTIVE)) {
            throw new AppException(ErrorCode.TRAY_CONFLICT, "Khay này đang có hợp đồng thuê hoạt động khác.");
        }

        // 5. Tiêu thụ mã & chuyển khay sang IN_USE
        Instant now = Instant.now();
        leaseTray.setLinkCodeUsedAt(now);
        leaseTray.setLinkedAt(now);
        leaseTrayRepository.save(leaseTray);

        tray.setStatus(TrayStatus.IN_USE);
        trayRepository.save(tray);

        // 6. Kiểm tra xem tất cả khay trong đơn đã được liên kết chưa
        List<LeaseTray> allTraysInLease = leaseTrayRepository.findByLeaseId(lease.getId());
        boolean allLinked = allTraysInLease.stream().allMatch(LeaseTray::isCodeUsed);

        if (allLinked) {
            lease.setStatus(LeaseStatus.ACTIVE);
            lease.setStartDate(now);
            RentalPackage pkg = packageRepository.findById(lease.getPackageId()).orElse(null);
            int duration = pkg != null ? pkg.getDurationDays() : 30;
            lease.setEndDate(now.plus(duration, ChronoUnit.DAYS));
            leaseRepository.save(lease);
        }

        return MyTrayResponse.fromEntity(leaseTray);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyTrayResponse> getMyActiveTrays(UUID customerId) {
        return leaseTrayRepository.findCustomerActiveTrays(customerId, LeaseStatus.ACTIVE).stream()
                .map(MyTrayResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MyTrayResponse getMyTrayDetail(UUID customerId, UUID trayId) {
        LeaseTray leaseTray = leaseTrayRepository.findCustomerActiveTray(trayId, customerId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAY_NOT_FOUND, "Không tìm thấy khay thuê đang hoạt động của bạn."));

        return MyTrayResponse.fromEntity(leaseTray);
    }

    private String generateLinkCode() {
        int number = 100000 + secureRandom.nextInt(900000);
        return "MUSH-" + number;
    }
}
