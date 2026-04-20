-- =====================================================================
--  Script tạo CSDL cho ứng dụng Quản lý hàng hóa Client-Server TCP
--  Chạy trong phpMyAdmin (XAMPP) hoặc MySQL Workbench:
--    - Mở http://localhost/phpmyadmin
--    - Tab SQL -> paste toàn bộ file này -> Go
-- =====================================================================

-- 1. Tạo database
CREATE DATABASE IF NOT EXISTS qlhanghoa
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE qlhanghoa;

-- 2. Tạo bảng hanghoa
DROP TABLE IF EXISTS hanghoa;
CREATE TABLE hanghoa (
    maHH     VARCHAR(20)  NOT NULL,
    tenHH    VARCHAR(255) NOT NULL,
    donGia   DOUBLE       NOT NULL DEFAULT 0,
    soLuong  INT          NOT NULL DEFAULT 0,
    loaiHH   VARCHAR(100) NOT NULL,
    PRIMARY KEY (maHH)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Dữ liệu mẫu
INSERT INTO hanghoa (maHH, tenHH, donGia, soLuong, loaiHH) VALUES
    ('MH001', 'Bút bi Thiên Long',  5000,  200, 'Văn phòng phẩm'),
    ('MH002', 'Sữa tươi Vinamilk', 32000,   50, 'Thực phẩm'),
    ('MH003', 'Áo thun nam',      150000,   30, 'Thời trang');

-- 4. Kiểm tra
SELECT * FROM hanghoa;
