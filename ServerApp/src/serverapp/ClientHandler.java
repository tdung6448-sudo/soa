package serverapp;

import common.HangHoa;
import common.Request;
import common.Response;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

/**
 * Thread xử lý 1 kết nối Client.
 *
 * Mỗi Client kết nối tới Server sẽ được ServerFrame tạo ra một ClientHandler
 * và chạy trong Thread riêng để phục vụ nhiều Client đồng thời.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final HangHoaService service;
    private final ServerFrame ui;
    private final String clientId;

    public ClientHandler(Socket socket, HangHoaService service, ServerFrame ui) {
        this.socket = socket;
        this.service = service;
        this.ui = ui;
        this.clientId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    @Override
    public void run() {
        // LƯU Ý: khởi tạo ObjectOutputStream TRƯỚC ObjectInputStream
        // vì ObjectInputStream sẽ chặn (block) chờ stream header.
        // try-with-resources giúp đóng stream và socket đúng cách.
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            ui.log("[+] Client kết nối: " + clientId);

            while (!socket.isClosed()) {
                // Đọc Request từ Client (blocking I/O)
                Request req = (Request) in.readObject();
                if (req == null || Request.CMD_EXIT.equalsIgnoreCase(req.getCommand())) {
                    ui.log("[-] Client " + clientId + " gửi EXIT, đóng kết nối.");
                    break;
                }

                ui.log("[>] " + clientId + " -> " + req.getCommand()
                        + (req.getKeyword() != null ? " (" + req.getKeyword() + ")" : "")
                        + (req.getHangHoa() != null ? " [" + req.getHangHoa().getMaHH() + "]" : ""));

                Response res = xuLy(req);

                // Gửi Response về cho Client
                out.writeObject(res);
                out.flush();
                // reset() tránh ObjectOutputStream cache object cũ,
                // dẫn đến Client nhận được dữ liệu không cập nhật.
                out.reset();

                ui.log("[<] " + clientId + " <- " + (res.isSuccess() ? "OK" : "FAIL") + ": " + res.getMessage());
            }

        } catch (SocketException se) {
            ui.log("[!] Kết nối " + clientId + " bị ngắt: " + se.getMessage());
        } catch (IOException | ClassNotFoundException ex) {
            ui.log("[!] Lỗi xử lý client " + clientId + ": " + ex.getMessage());
        } finally {
            try {
                if (!socket.isClosed()) socket.close();
            } catch (IOException ignored) {}
            ui.log("[x] Đã đóng kết nối " + clientId);
        }
    }

    /** Định tuyến command tới phương thức service tương ứng. */
    private Response xuLy(Request req) {
        String cmd = req.getCommand() == null ? "" : req.getCommand().toUpperCase();
        switch (cmd) {
            case Request.CMD_ADD:
                if (req.getHangHoa() == null) {
                    return new Response(false, "Dữ liệu hàng hóa rỗng");
                }
                boolean okAdd = service.them(req.getHangHoa());
                return new Response(okAdd,
                        okAdd ? "Thêm hàng hóa thành công" : "Mã hàng đã tồn tại hoặc dữ liệu lỗi");

            case Request.CMD_VIEW:
                List<HangHoa> all = service.xemTatCa();
                return new Response(true, "Có " + all.size() + " hàng hóa", all);

            case Request.CMD_SEARCH:
                List<HangHoa> result = service.timKiem(req.getKeyword());
                return new Response(true, "Tìm thấy " + result.size() + " kết quả", result);

            case Request.CMD_UPDATE:
                if (req.getHangHoa() == null) {
                    return new Response(false, "Dữ liệu hàng hóa rỗng");
                }
                boolean okUpd = service.capNhat(req.getHangHoa());
                return new Response(okUpd,
                        okUpd ? "Cập nhật thành công" : "Không tìm thấy mã hàng để cập nhật");

            case Request.CMD_DELETE:
                boolean okDel = service.xoa(req.getKeyword());
                return new Response(okDel,
                        okDel ? "Xóa thành công" : "Không tìm thấy mã hàng để xóa");

            default:
                return new Response(false, "Lệnh không hợp lệ: " + cmd);
        }
    }
}
