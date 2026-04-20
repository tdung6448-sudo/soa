package clientapp;

import common.HangHoa;
import common.Request;
import common.Response;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện Client:
 *  - Kết nối Server (IP, Port).
 *  - Form nhập: Mã, Tên, Đơn giá, Số lượng, Loại.
 *  - Các nút: Thêm, Cập nhật, Xóa, Tìm kiếm, Xem tất cả.
 *  - JTable hiển thị danh sách hàng hóa từ Server trả về.
 */
public class ClientFrame extends JFrame {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 9999;

    private final JTextField txtHost = new JTextField(DEFAULT_HOST, 12);
    private final JTextField txtPort = new JTextField(String.valueOf(DEFAULT_PORT), 6);
    private final JButton btnConnect    = new JButton("Kết nối");
    private final JButton btnDisconnect = new JButton("Ngắt kết nối");
    private final JLabel lblStatus = new JLabel("Chưa kết nối");

    private final JTextField txtMa   = new JTextField();
    private final JTextField txtTen  = new JTextField();
    private final JTextField txtGia  = new JTextField();
    private final JTextField txtSL   = new JTextField();
    private final JTextField txtLoai = new JTextField();

    private final JButton btnThem    = new JButton("Thêm");
    private final JButton btnCapNhat = new JButton("Cập nhật");
    private final JButton btnXoa     = new JButton("Xóa");
    private final JButton btnXemAll  = new JButton("Xem tất cả");
    private final JTextField txtSearch = new JTextField(15);
    private final JButton btnSearch  = new JButton("Tìm kiếm");

    private final String[] columns = {"Mã HH", "Tên hàng", "Đơn giá", "Số lượng", "Loại"};
    private final DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final SocketClient client = new SocketClient();

    public ClientFrame() {
        setTitle("Client quản lý hàng hóa - TCP Socket");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        add(buildConnectionPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(),     BorderLayout.CENTER);

        // Khi đóng cửa sổ, đóng socket
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                client.close();
            }
        });

        // Khi chọn 1 hàng trong bảng, đổ dữ liệu vào form
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromSelectedRow());

        updateButtons(false);
    }

    private JPanel buildConnectionPanel() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnl.setBorder(BorderFactory.createTitledBorder("Kết nối Server"));
        pnl.add(new JLabel("IP:"));
        pnl.add(txtHost);
        pnl.add(new JLabel("Port:"));
        pnl.add(txtPort);
        pnl.add(btnConnect);
        pnl.add(btnDisconnect);
        pnl.add(Box.createRigidArea(new Dimension(20, 0)));
        pnl.add(lblStatus);

        btnConnect.addActionListener(e -> doConnect());
        btnDisconnect.addActionListener(e -> doDisconnect());
        return pnl;
    }

    private JComponent buildCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(5, 5));

        // Form nhập liệu
        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.setBorder(new TitledBorder("Thông tin hàng hóa"));
        form.add(new JLabel("Mã hàng:"));     form.add(txtMa);
        form.add(new JLabel("Tên hàng:"));    form.add(txtTen);
        form.add(new JLabel("Đơn giá:"));     form.add(txtGia);
        form.add(new JLabel("Số lượng:"));    form.add(txtSL);
        form.add(new JLabel("Loại:"));        form.add(txtLoai);

        // Các nút chức năng
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setBorder(new TitledBorder("Chức năng"));
        buttons.add(btnThem);
        buttons.add(btnCapNhat);
        buttons.add(btnXoa);
        buttons.add(btnXemAll);
        buttons.add(new JLabel("  Từ khóa:"));
        buttons.add(txtSearch);
        buttons.add(btnSearch);

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        // Bảng kết quả
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new TitledBorder("Danh sách hàng hóa"));

        center.add(top, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        btnThem.addActionListener(e -> doThem());
        btnCapNhat.addActionListener(e -> doCapNhat());
        btnXoa.addActionListener(e -> doXoa());
        btnXemAll.addActionListener(e -> doXemAll());
        btnSearch.addActionListener(e -> doSearch());

        return center;
    }

    // ========= Xử lý kết nối =========

    private void doConnect() {
        String host = txtHost.getText().trim();
        int port;
        try {
            port = Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException ex) {
            err("Port không hợp lệ"); return;
        }
        if (host.isEmpty()) { err("IP rỗng"); return; }

        try {
            client.connect(host, port);
            lblStatus.setText("Đã kết nối " + host + ":" + port);
            lblStatus.setForeground(new Color(0, 128, 0));
            updateButtons(true);
            doXemAll();
        } catch (Exception ex) {
            err("Không kết nối được Server: " + ex.getMessage());
        }
    }

    private void doDisconnect() {
        client.close();
        lblStatus.setText("Đã ngắt kết nối");
        lblStatus.setForeground(Color.BLACK);
        updateButtons(false);
        tableModel.setRowCount(0);
    }

    private void updateButtons(boolean connected) {
        btnConnect.setEnabled(!connected);
        btnDisconnect.setEnabled(connected);
        btnThem.setEnabled(connected);
        btnCapNhat.setEnabled(connected);
        btnXoa.setEnabled(connected);
        btnXemAll.setEnabled(connected);
        btnSearch.setEnabled(connected);
    }

    // ========= Các chức năng gọi Server =========

    private void doThem() {
        HangHoa hh = docFormValidate();
        if (hh == null) return;
        sendRequest(new Request(Request.CMD_ADD, hh), true);
    }

    private void doCapNhat() {
        HangHoa hh = docFormValidate();
        if (hh == null) return;
        sendRequest(new Request(Request.CMD_UPDATE, hh), true);
    }

    private void doXoa() {
        String ma = txtMa.getText().trim();
        if (ma.isEmpty()) { err("Nhập mã hàng cần xóa"); return; }
        int c = JOptionPane.showConfirmDialog(this, "Xóa mã " + ma + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        sendRequest(new Request(Request.CMD_DELETE, ma), true);
    }

    private void doXemAll() {
        sendRequest(new Request(Request.CMD_VIEW), false);
    }

    private void doSearch() {
        String kw = txtSearch.getText().trim();
        if (kw.isEmpty()) { err("Nhập từ khóa tìm kiếm"); return; }
        sendRequest(new Request(Request.CMD_SEARCH, kw), false);
    }

    /** Gửi request, nhận response, cập nhật bảng/thông báo.
     *  reloadAfter = true sẽ gọi VIEW sau khi thao tác thay đổi dữ liệu.
     */
    private void sendRequest(Request req, boolean reloadAfter) {
        // Thực hiện trên thread riêng để không chặn giao diện
        new Thread(() -> {
            try {
                Response res = client.send(req);
                SwingUtilities.invokeLater(() -> {
                    if (res.getData() != null && !res.getData().isEmpty()) {
                        loadTable(res.getData());
                    } else if (!reloadAfter) {
                        tableModel.setRowCount(0);
                    }
                    if (res.isSuccess()) {
                        info(res.getMessage());
                    } else {
                        err(res.getMessage());
                    }
                });
                if (reloadAfter && res.isSuccess()) {
                    // Sau ADD/UPDATE/DELETE thành công, lấy lại danh sách
                    Response all = client.send(new Request(Request.CMD_VIEW));
                    SwingUtilities.invokeLater(() -> loadTable(all.getData()));
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    err("Lỗi giao tiếp Server: " + ex.getMessage())
                );
            }
        }, "ClientRequestThread").start();
    }

    // ========= Hàm tiện ích =========

    private HangHoa docFormValidate() {
        String ma   = txtMa.getText().trim();
        String ten  = txtTen.getText().trim();
        String gia  = txtGia.getText().trim();
        String sl   = txtSL.getText().trim();
        String loai = txtLoai.getText().trim();

        if (ma.isEmpty())   { err("Mã hàng không được rỗng"); return null; }
        if (ten.isEmpty())  { err("Tên hàng không được rỗng"); return null; }
        if (loai.isEmpty()) { err("Loại không được rỗng"); return null; }
        double donGia;
        int soLuong;
        try { donGia = Double.parseDouble(gia); }
        catch (NumberFormatException ex) { err("Đơn giá phải là số"); return null; }
        if (donGia < 0) { err("Đơn giá không được âm"); return null; }
        try { soLuong = Integer.parseInt(sl); }
        catch (NumberFormatException ex) { err("Số lượng phải là số nguyên"); return null; }
        if (soLuong < 0) { err("Số lượng không được âm"); return null; }

        return new HangHoa(ma, ten, donGia, soLuong, loai);
    }

    private void loadTable(List<HangHoa> list) {
        tableModel.setRowCount(0);
        if (list == null) return;
        for (HangHoa hh : list) {
            tableModel.addRow(new Object[]{
                hh.getMaHH(), hh.getTenHH(), hh.getDonGia(), hh.getSoLuong(), hh.getLoaiHH()
            });
        }
    }

    private void fillFormFromSelectedRow() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        txtMa.setText(String.valueOf(tableModel.getValueAt(r, 0)));
        txtTen.setText(String.valueOf(tableModel.getValueAt(r, 1)));
        txtGia.setText(String.valueOf(tableModel.getValueAt(r, 2)));
        txtSL.setText(String.valueOf(tableModel.getValueAt(r, 3)));
        txtLoai.setText(String.valueOf(tableModel.getValueAt(r, 4)));
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
