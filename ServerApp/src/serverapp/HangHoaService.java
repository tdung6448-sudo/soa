package serverapp;

import common.HangHoa;
import java.util.ArrayList;
import java.util.List;

/**
 * Service quản lý danh sách hàng hóa ở phía Server.
 * Dữ liệu lưu trong ArrayList<HangHoa>.
 *
 * Các phương thức được đồng bộ (synchronized) vì có nhiều Thread
 * (mỗi Client một Thread) có thể truy cập đồng thời.
 */
public class HangHoaService {

    private final List<HangHoa> danhSach = new ArrayList<>();

    public HangHoaService() {
        // Dữ liệu mẫu ban đầu
        danhSach.add(new HangHoa("MH001", "Bút bi Thiên Long", 5000, 200, "Văn phòng phẩm"));
        danhSach.add(new HangHoa("MH002", "Sữa tươi Vinamilk", 32000, 50, "Thực phẩm"));
        danhSach.add(new HangHoa("MH003", "Áo thun nam", 150000, 30, "Thời trang"));
    }

    /** Thêm hàng hóa mới, trả về false nếu mã đã tồn tại. */
    public synchronized boolean them(HangHoa hh) {
        if (hh == null || hh.getMaHH() == null) return false;
        if (timTheoMa(hh.getMaHH()) != null) return false;
        danhSach.add(hh);
        return true;
    }

    /** Lấy toàn bộ danh sách (copy để tránh sửa đổi ngoài ý muốn). */
    public synchronized List<HangHoa> xemTatCa() {
        return new ArrayList<>(danhSach);
    }

    /** Tìm chính xác theo mã. */
    public synchronized HangHoa timTheoMa(String maHH) {
        if (maHH == null) return null;
        for (HangHoa hh : danhSach) {
            if (hh.getMaHH().equalsIgnoreCase(maHH)) {
                return hh;
            }
        }
        return null;
    }

    /** Tìm kiếm theo mã hoặc tên (không phân biệt hoa thường, chứa chuỗi). */
    public synchronized List<HangHoa> timKiem(String keyword) {
        List<HangHoa> ketQua = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return ketQua;
        String key = keyword.toLowerCase().trim();
        for (HangHoa hh : danhSach) {
            if (hh.getMaHH().toLowerCase().contains(key)
                    || hh.getTenHH().toLowerCase().contains(key)) {
                ketQua.add(hh);
            }
        }
        return ketQua;
    }

    /** Cập nhật thông tin hàng hóa theo mã. */
    public synchronized boolean capNhat(HangHoa hh) {
        if (hh == null) return false;
        HangHoa cu = timTheoMa(hh.getMaHH());
        if (cu == null) return false;
        cu.setTenHH(hh.getTenHH());
        cu.setDonGia(hh.getDonGia());
        cu.setSoLuong(hh.getSoLuong());
        cu.setLoaiHH(hh.getLoaiHH());
        return true;
    }

    /** Xóa hàng hóa theo mã. */
    public synchronized boolean xoa(String maHH) {
        HangHoa hh = timTheoMa(maHH);
        if (hh == null) return false;
        return danhSach.remove(hh);
    }
}
