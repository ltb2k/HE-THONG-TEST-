-- ====================================================================
-- V3__rental_schema.sql: Khởi tạo bảng Dịch vụ thuê khay (Rental & Lease)
-- ====================================================================

-- Bảng gói thuê
CREATE TABLE IF NOT EXISTS rental_packages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price_vnd NUMERIC(12, 2) NOT NULL,
    duration_days INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'HIDDEN')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_rental_packages_status ON rental_packages(status);

-- Bảng hợp đồng / yêu cầu thuê (Lease)
CREATE TABLE IF NOT EXISTS leases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES users(id),
    package_id UUID NOT NULL REFERENCES rental_packages(id),
    quantity INT NOT NULL CHECK (quantity >= 1 AND quantity <= 5),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'ASSIGNED', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    cancel_reason TEXT,
    start_date TIMESTAMP WITH TIME ZONE,
    end_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_leases_customer_id ON leases(customer_id);
CREATE INDEX IF NOT EXISTS idx_leases_status ON leases(status);

-- Bảng gán khay vào đơn thuê & mã liên kết (BR-RENT-001)
CREATE TABLE IF NOT EXISTS lease_trays (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lease_id UUID NOT NULL REFERENCES leases(id) ON DELETE CASCADE,
    tray_id UUID NOT NULL REFERENCES trays(id),
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    link_code VARCHAR(50) UNIQUE,
    link_code_expires_at TIMESTAMP WITH TIME ZONE,
    link_code_used_at TIMESTAMP WITH TIME ZONE,
    linked_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_lease_trays_lease_id ON lease_trays(lease_id);
CREATE INDEX IF NOT EXISTS idx_lease_trays_tray_id ON lease_trays(tray_id);
CREATE INDEX IF NOT EXISTS idx_lease_trays_link_code ON lease_trays(link_code);

-- Bảng giao dịch thanh toán mô phỏng (BR-RENT-002: SIMULATED)
CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lease_id UUID NOT NULL REFERENCES leases(id) ON DELETE CASCADE,
    amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SIMULATED' CHECK (status IN ('SIMULATED', 'SIMULATED_CONFIRMED', 'SIMULATED_REJECTED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_transactions_lease_id ON transactions(lease_id);
