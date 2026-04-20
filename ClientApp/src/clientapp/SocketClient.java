package clientapp;

import common.Request;
import common.Response;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Lớp bao bọc Socket Client.
 *
 * Mở 1 kết nối tới Server và giữ nó mở (persistent connection) để
 * gửi nhiều request liên tiếp. Mỗi lần gọi send() gửi 1 Request
 * và chờ nhận lại 1 Response.
 */
public class SocketClient {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    /**
     * Mở kết nối TCP tới Server.
     * Khi gọi new Socket(host, port) thì tầng TCP thực hiện 3-way handshake:
     *   Client --SYN--> Server
     *   Client <--SYN+ACK-- Server
     *   Client --ACK--> Server
     * Sau khi handshake thành công, constructor trả về.
     */
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        // Thứ tự rất quan trọng: mở OutputStream trước, flush, rồi mới mở InputStream
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    /** Gửi request, chờ response. Đồng bộ để tránh 2 thread cùng gửi đè nhau. */
    public synchronized Response send(Request req) throws IOException, ClassNotFoundException {
        if (socket == null || socket.isClosed()) {
            throw new IOException("Chưa kết nối tới Server");
        }
        out.writeObject(req);
        out.flush();
        out.reset();
        return (Response) in.readObject();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /** Đóng kết nối an toàn (gửi EXIT trước rồi đóng socket). */
    public void close() {
        try {
            if (isConnected()) {
                try {
                    send(new Request(Request.CMD_EXIT));
                } catch (Exception ignored) {}
            }
        } finally {
            try { if (in != null) in.close(); } catch (IOException ignored) {}
            try { if (out != null) out.close(); } catch (IOException ignored) {}
            try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        }
    }
}
