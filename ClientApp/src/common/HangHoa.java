package common;

import java.io.Serializable;

/**
 * Lớp mô tả đối tượng Hàng hóa.
 *
 * Cài đặt Serializable để có thể truyền object qua Socket
 * bằng ObjectInputStream / ObjectOutputStream.
 *
 * Class này phải NẰM CÙNG PACKAGE VÀ CÓ NỘI DUNG GIỐNG NHAU
 * ở cả Server và Client thì Java mới deserialize được.
 */
public class HangHoa implements Serializable {

    private static final long serialVersionUID = 1L;

    private String maHH;
    private String tenHH;
    private double donGia;
    private int soLuong;
    private String loaiHH;

    public HangHoa() {
    }

    public HangHoa(String maHH, String tenHH, double donGia, int soLuong, String loaiHH) {
        this.maHH = maHH;
        this.tenHH = tenHH;
        this.donGia = donGia;
        this.soLuong = soLuong;
        this.loaiHH = loaiHH;
    }

    public String getMaHH() { return maHH; }
    public void setMaHH(String maHH) { this.maHH = maHH; }

    public String getTenHH() { return tenHH; }
    public void setTenHH(String tenHH) { this.tenHH = tenHH; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public String getLoaiHH() { return loaiHH; }
    public void setLoaiHH(String loaiHH) { this.loaiHH = loaiHH; }

    @Override
    public String toString() {
        return maHH + " - " + tenHH + " - " + donGia + " - " + soLuong + " - " + loaiHH;
    }
}
