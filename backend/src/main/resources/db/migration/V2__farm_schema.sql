-- ====================================================================
-- V2__farm_schema.sql: Khởi tạo bảng Phòng trồng (GrowRoom) & Khay trồng (Tray)
-- ====================================================================

-- Bảng phòng trồng
CREATE TABLE IF NOT EXISTS grow_rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_grow_rooms_code ON grow_rooms(code);
CREATE INDEX IF NOT EXISTS idx_grow_rooms_status ON grow_rooms(status);

-- Bảng khay trồng
CREATE TABLE IF NOT EXISTS trays (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unique_code VARCHAR(50) NOT NULL UNIQUE,
    room_id UUID NOT NULL REFERENCES grow_rooms(id),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'ASSIGNED', 'IN_USE', 'MAINTENANCE', 'ARCHIVED')),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_trays_unique_code ON trays(unique_code);
CREATE INDEX IF NOT EXISTS idx_trays_room_id ON trays(room_id);
CREATE INDEX IF NOT EXISTS idx_trays_status ON trays(status);
