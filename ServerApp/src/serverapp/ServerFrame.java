package serverapp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

/**
 * Giao diện Server:
 * - Hiển thị cổng (port) đang lắng nghe.
 * - Hai nút Start / Stop.
 * - Một JTextArea hiển thị log kết nối và giao dịch.
 *
 * Server chạy trong Thread riêng (acceptThread) để không chặn giao diện.
 */
public class ServerFrame extends JFrame {

    private static final int DEFAULT_PORT = 9999;

    private final JTextField txtPort = new JTextField(String.valueOf(DEFAULT_PORT), 6);
    private final JButton btnStart  = new JButton("Start");
    private final JButton btnStop   = new JButton("Stop");
    private final JTextArea txtLog  = new JTextArea();
    private final JLabel lblStatus  = new JLabel("Trạng thái: đang dừng");

    private final HangHoaService service = new HangHoaService();
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running = false;

    public ServerFrame() {
        setTitle("Server quản lý hàng hóa - TCP Socket");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);

        // Panel điều khiển phía trên
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlTop.add(new JLabel("Port:"));
        pnlTop.add(txtPort);
        pnlTop.add(btnStart);
        pnlTop.add(btnStop);
        pnlTop.add(Box.createRigidArea(new Dimension(20, 0)));
        pnlTop.add(lblStatus);

        // Vùng log
        txtLog.setEditable(false);
        txtLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(txtLog);

        add(pnlTop, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        btnStart.addActionListener(e -> startServer());
        btnStop.addActionListener(e -> stopServer());
        btnStop.setEnabled(false);
    }

    /** Ghi log có thời gian, đảm bảo chạy trên EDT. */
    public void log(String msg) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        SwingUtilities.invokeLater(() -> {
            txtLog.append("[" + time + "] " + msg + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    private void startServer() {
        int port;
        try {
            port = Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Port không hợp lệ", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
            running = true;
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
            txtPort.setEnabled(false);
            lblStatus.setText("Trạng thái: đang lắng nghe tại port " + port);
            log("Server khởi động tại port " + port);

            // Thread nhận kết nối - không chặn EDT
            acceptThread = new Thread(this::acceptLoop, "AcceptThread");
            acceptThread.setDaemon(true);
            acceptThread.start();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không mở được port: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acceptLoop() {
        while (running && !serverSocket.isClosed()) {
            try {
                // accept() = hoàn tất 3-way handshake phía Server
                Socket clientSocket = serverSocket.accept();
                // Mỗi Client một Thread riêng -> phục vụ đa Client
                Thread t = new Thread(new ClientHandler(clientSocket, service, this));
                t.setDaemon(true);
                t.start();
            } catch (IOException ex) {
                if (running) log("Lỗi accept: " + ex.getMessage());
            }
        }
    }

    private void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ex) {
            log("Lỗi khi dừng server: " + ex.getMessage());
        }
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        txtPort.setEnabled(true);
        lblStatus.setText("Trạng thái: đang dừng");
        log("Server đã dừng.");
    }
}
