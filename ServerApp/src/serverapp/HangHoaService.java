package serverapp;

import common.HangHoa;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service quản lý hàng hóa — KẾT NỐI MYSQL (XAMPP) qua JDBC.
 *
 * Mỗi phương thức mở 1 Connection ngắn, dùng try-with-resources
 * để đảm bảo đóng Connection/Statement/ResultSet đúng cách.
 * PreparedStatement được dùng để tránh SQL Injection.
 */
public class HangHoaService {

    public HangHoaService() {
        // Kiểm tra kết nối DB ngay khi khởi động Server.
        try (Connection c = DBConfig.open()) {
            System.out.println("Đã kết nối MySQL: " + DBConfig.URL);
        } catch (SQLException ex) {
            throw new RuntimeException(
                "Không kết nối được MySQL. Kiểm tra XAMPP đã Start MySQL và chạy db/schema.sql chưa. "
                + "Chi tiết: " + ex.getMessage(), ex);
        }
    }

    /** Thêm hàng hóa. Trả về false nếu mã trùng (PRIMARY KEY) hoặc lỗi. */
    public boolean them(HangHoa hh) {
        if (hh == null || hh.getMaHH() == null) return false;
        String sql = "INSERT INTO hanghoa(maHH, tenHH, donGia, soLuong, loaiHH) VALUES (?,?,?,?,?)";
        try (Connection c = DBConfig.open();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hh.getMaHH());
            ps.setString(2, hh.getTenHH());
            ps.setDouble(3, hh.getDonGia());
            ps.setInt   (4, hh.getSoLuong());
            ps.setString(5, hh.getLoaiHH());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            // Trùng PK sẽ ném SQLIntegrityConstraintViolationException
            System.err.println("[them] " + ex.getMessage());
            return false;
        }
    }

    /** Lấy toàn bộ danh sách. */
    public List<HangHoa> xemTatCa() {
        String sql = "SELECT maHH, tenHH, donGia, soLuong, loaiHH FROM hanghoa ORDER BY maHH";
        List<HangHoa> ds = new ArrayList<>();
        try (Connection c = DBConfig.open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(map(rs));
        } catch (SQLException ex) {
            System.err.println("[xemTatCa] " + ex.getMessage());
        }
        return ds;
    }

    /** Tìm chính xác theo mã. Trả null nếu không có. */
    public HangHoa timTheoMa(String maHH) {
        if (maHH == null) return null;
        String sql = "SELECT maHH, tenHH, donGia, soLuong, loaiHH FROM hanghoa WHERE maHH = ?";
        try (Connection c = DBConfig.open();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maHH);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException ex) {
            System.err.println("[timTheoMa] " + ex.getMessage());
        }
        return null;
    }

    /** Tìm kiếm LIKE theo mã hoặc tên, không phân biệt hoa/thường. */
    public List<HangHoa> timKiem(String keyword) {
        List<HangHoa> ds = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return ds;
        String sql = "SELECT maHH, tenHH, donGia, soLuong, loaiHH FROM hanghoa "
                   + "WHERE LOWER(maHH) LIKE ? OR LOWER(tenHH) LIKE ? ORDER BY maHH";
        String like = "%" + keyword.toLowerCase().trim() + "%";
        try (Connection c = DBConfig.open();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(map(rs));
            }
        } catch (SQLException ex) {
            System.err.println("[timKiem] " + ex.getMessage());
        }
        return ds;
    }

    /** Cập nhật theo mã. */
    public boolean capNhat(HangHoa hh) {
        if (hh == null) return false;
        String sql = "UPDATE hanghoa SET tenHH=?, donGia=?, soLuong=?, loaiHH=? WHERE maHH=?";
        try (Connection c = DBConfig.open();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hh.getTenHH());
            ps.setDouble(2, hh.getDonGia());
            ps.setInt   (3, hh.getSoLuong());
            ps.setString(4, hh.getLoaiHH());
            ps.setString(5, hh.getMaHH());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[capNhat] " + ex.getMessage());
            return false;
        }
    }

    /** Xóa theo mã. */
    public boolean xoa(String maHH) {
        if (maHH == null) return false;
        String sql = "DELETE FROM hanghoa WHERE maHH = ?";
        try (Connection c = DBConfig.open();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maHH);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("[xoa] " + ex.getMessage());
            return false;
        }
    }

    /** Map 1 dòng ResultSet sang HangHoa. */
    private static HangHoa map(ResultSet rs) throws SQLException {
        return new HangHoa(
            rs.getString("maHH"),
            rs.getString("tenHH"),
            rs.getDouble("donGia"),
            rs.getInt   ("soLuong"),
            rs.getString("loaiHH")
        );
    }
}
