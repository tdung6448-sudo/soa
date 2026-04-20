# Quản lý hàng hóa – Client/Server TCP Socket (Java – NetBeans)

Dự án minh họa ứng dụng Client-Server lập trình Socket TCP bằng Java.
Gồm **2 project NetBeans** độc lập:

```
soa/
├── ServerApp/     # Ứng dụng Server (JFrame Swing)
└── ClientApp/     # Ứng dụng Client (JFrame Swing)
```

Mỗi project đều có package `common` chứa 3 class dùng chung
(`HangHoa`, `Request`, `Response`). **Nội dung của 3 class này PHẢI
GIỐNG HỆT NHAU** ở hai bên – vì Java serialization dựa trên
`fully-qualified class name + serialVersionUID + cấu trúc field`.

---

## 1. Cấu trúc dữ liệu hàng hóa

```java
public class HangHoa implements Serializable {
    private String maHH;    // Mã hàng hóa
    private String tenHH;   // Tên hàng hóa
    private double donGia;  // Đơn giá
    private int    soLuong; // Số lượng tồn kho
    private String loaiHH;  // Loại hàng hóa
    // + constructors, getters/setters
}
```

`implements Serializable` là điều kiện bắt buộc để object được
`ObjectOutputStream.writeObject()` ghi ra socket.

---

## 2. Cơ chế hoạt động của Socket TCP

### 2.1. Mở kết nối – 3-way handshake

Khi Client gọi `new Socket(host, port)` và Server đang `accept()`,
tầng TCP của hệ điều hành tự động thực hiện bắt tay 3 bước:

```
 Client                                              Server
   │                                                    │
   │ ────── SYN   (seq=x)                 ─────────▶    │   (1) Client yêu cầu mở kết nối
   │                                                    │
   │ ◀───── SYN + ACK (seq=y, ack=x+1)    ─────────     │   (2) Server đồng ý
   │                                                    │
   │ ────── ACK   (ack=y+1)               ─────────▶    │   (3) Client xác nhận
   │                                                    │
   │  ═══════════  ESTABLISHED  ═══════════             │
   │                                                    │
```

Sau bước (3), socket ở trạng thái **ESTABLISHED** – hai bên có thể
truyền dữ liệu 2 chiều, có thứ tự, tin cậy (TCP tự retransmit).

Trong Java:

```java
// Phía Server
ServerSocket ss = new ServerSocket(9999);
Socket client   = ss.accept();   // ← chặn đến khi handshake xong

// Phía Client
Socket s = new Socket("127.0.0.1", 9999);  // ← handshake ngay trong constructor
```

### 2.2. Truyền object qua stream

```
 Client                                              Server
   │                                                    │
   │ writeObject(Request{"ADD", HangHoa})                │
   │ ──────── bytes qua TCP ──────────────▶             │
   │                                          readObject() → Request
   │                                          xử lý nghiệp vụ (ArrayList)
   │                                          writeObject(Response{...})
   │ ◀──────── bytes qua TCP ──────────────              │
   │ readObject() → Response                             │
   │ cập nhật JTable                                     │
```

`ObjectOutputStream` nối tiếp object thành byte, `ObjectInputStream`
dựng lại object phía kia. TCP đảm bảo từng byte đến đúng thứ tự,
không mất gói (nếu mất, TCP tự gửi lại).

**Lưu ý quan trọng về thứ tự mở stream**: luôn mở
`ObjectOutputStream` trước rồi mới `ObjectInputStream`. Nếu làm
ngược, `ObjectInputStream` sẽ chặn mãi chờ stream header mà phía
kia không thể gửi được.

### 2.3. Đóng kết nối – 4-way handshake

Khi Client gửi `Request("EXIT")` hoặc đóng socket, TCP tiến hành
FIN → ACK → FIN → ACK để đóng 2 chiều.

### 2.4. Hỗ trợ nhiều Client đồng thời

Server chạy vòng lặp `accept()` trong **Thread riêng**
(`AcceptThread`). Mỗi lần `accept()` trả về, Server tạo một
`ClientHandler` và bọc trong **Thread mới** – mỗi Client là một
luồng độc lập, không chặn lẫn nhau. Truy cập vào
`ArrayList<HangHoa>` được bảo vệ bởi `synchronized` trong
`HangHoaService` để tránh race condition.

---

## 3. Giao thức ứng dụng

Dùng **ObjectStream** truyền hai loại object:

### Request (Client → Server)

| Trường    | Ý nghĩa                                             |
|-----------|-----------------------------------------------------|
| command   | `ADD` / `VIEW` / `SEARCH` / `UPDATE` / `DELETE` / `EXIT` |
| keyword   | Chuỗi tìm kiếm hoặc mã hàng (cho DELETE/SEARCH)     |
| hangHoa   | Đối tượng `HangHoa` đi kèm (cho ADD/UPDATE)         |

### Response (Server → Client)

| Trường    | Ý nghĩa                                      |
|-----------|----------------------------------------------|
| success   | `true` nếu xử lý thành công                  |
| message   | Thông điệp cho người dùng                    |
| data      | `List<HangHoa>` kết quả (VIEW / SEARCH)      |

### Bảng hành vi

| Lệnh    | Request                       | Phía Server làm gì                | Response                       |
|---------|-------------------------------|-----------------------------------|--------------------------------|
| VIEW    | `{command=VIEW}`              | `service.xemTatCa()`              | `data = tất cả hàng hóa`       |
| ADD     | `{command=ADD, hangHoa=...}`  | `service.them(hh)` (check trùng)  | `success=true/false`           |
| SEARCH  | `{command=SEARCH, keyword=}`  | `service.timKiem(keyword)`        | `data = danh sách khớp`        |
| UPDATE  | `{command=UPDATE, hangHoa=}`  | `service.capNhat(hh)`             | `success=true/false`           |
| DELETE  | `{command=DELETE, keyword=}`  | `service.xoa(keyword)`            | `success=true/false`           |
| EXIT    | `{command=EXIT}`              | đóng socket phía server           | (không trả lời)                |

> Có thể thay thế bằng định dạng chuỗi `COMMAND|DATA`
> (vd `ADD|MH001,SanPhamA,10000,50,LoaiA`) – nhưng object stream
> dễ bảo trì và an toàn kiểu hơn.

---

## 4. Hướng dẫn chạy trên NetBeans

### Yêu cầu
- **JDK 8 trở lên** (dự án đã thử biên dịch với JDK 21).
- **NetBeans 12+** (hoặc Apache NetBeans 17+).
- **XAMPP** (cho MySQL) — phần 4.0 bên dưới.

### 4.0. Chuẩn bị MySQL (XAMPP)

1. Mở **XAMPP Control Panel** → **Start** các dịch vụ **Apache** và **MySQL**.
2. Mở trình duyệt → http://localhost/phpmyadmin
3. Tab **SQL** → paste toàn bộ nội dung `db/schema.sql` → nhấn **Go**.
   Sẽ tạo database `qlhanghoa` + bảng `hanghoa` + 3 dòng mẫu.
4. Tải driver **MySQL Connector/J 8.x**:
   https://dev.mysql.com/downloads/connector/j/ (Platform Independent .zip).
   Giải nén, lấy `mysql-connector-j-8.x.x.jar` → đặt vào
   `ServerApp/lib/mysql-connector-j.jar` (đổi tên cho đúng).
5. Trong NetBeans, chuột phải **ServerApp → Properties → Libraries →
   Add JAR/Folder** → chọn file JAR vừa copy.

Nếu cần đổi user/pass MySQL, sửa trong
`ServerApp/src/serverapp/DBConfig.java` (mặc định XAMPP: `root` / rỗng).

### Các bước chạy

1. **Mở 2 project riêng**
   - NetBeans → `File > Open Project...` → chọn thư mục `ServerApp`.
   - Lặp lại: `File > Open Project...` → chọn thư mục `ClientApp`.

2. **Chạy Server trước**
   - Trong panel Projects, chuột phải **ServerApp → Run**.
   - Hoặc đặt Main class: `serverapp.ServerMain` rồi `Shift+F6`.
   - Cửa sổ Server mở, nhập port (mặc định **9999**) → nhấn **Start**.

3. **Chạy Client**
   - Chuột phải **ClientApp → Run** (main: `clientapp.ClientMain`).
   - Nhập IP (cùng máy: `127.0.0.1`, khác máy: IP LAN của server) và
     Port `9999` → nhấn **Kết nối**.

4. **Kiểm thử đa Client**: mở Client lần thứ 2 (`Run` lần nữa, hoặc
   bật cho phép Multiple Instances trong project properties) – cả
   hai Client đều kết nối được tới cùng một Server và cùng thao tác
   trên kho hàng.

### Chạy từ dòng lệnh (không cần NetBeans)

```bash
# Biên dịch
cd ServerApp && javac -d build $(find src -name '*.java') && cd ..
cd ClientApp && javac -d build $(find src -name '*.java') && cd ..

# Chạy
java -cp ServerApp/build serverapp.ServerMain   # cửa sổ 1
java -cp ClientApp/build clientapp.ClientMain   # cửa sổ 2
```

---

## 5. Mô tả giao diện

### Server

```
┌────────────────────────────────────────────────────────────┐
│ Server quản lý hàng hóa - TCP Socket                       │
├────────────────────────────────────────────────────────────┤
│ Port: [9999]  [ Start ]  [ Stop ]    Trạng thái: lắng nghe │
├────────────────────────────────────────────────────────────┤
│ [08:15:01] Server khởi động tại port 9999                  │
│ [08:15:12] [+] Client kết nối: 127.0.0.1:54321             │
│ [08:15:20] [>] 127.0.0.1:54321 -> VIEW                     │
│ [08:15:20] [<] 127.0.0.1:54321 <- OK: Có 3 hàng hóa        │
│ [08:15:45] [>] 127.0.0.1:54321 -> ADD [MH010]              │
│ [08:15:45] [<] 127.0.0.1:54321 <- OK: Thêm thành công      │
│ ...                                                        │
└────────────────────────────────────────────────────────────┘
```

### Client

```
┌────────────────────────────────────────────────────────────┐
│ Client quản lý hàng hóa - TCP Socket                       │
├────────────────────────────────────────────────────────────┤
│ Kết nối Server                                             │
│   IP: [127.0.0.1]  Port: [9999]  [Kết nối] [Ngắt]   OK    │
├────────────────────────────────────────────────────────────┤
│ Thông tin hàng hóa                                         │
│   Mã hàng:   [MH001]                                       │
│   Tên hàng:  [Bút bi Thiên Long]                           │
│   Đơn giá:   [5000]                                        │
│   Số lượng:  [200]                                         │
│   Loại:      [Văn phòng phẩm]                              │
│                                                            │
│ Chức năng:                                                 │
│   [Thêm] [Cập nhật] [Xóa] [Xem tất cả]  Từ khóa: [   ]    │
│                                                 [Tìm kiếm] │
├────────────────────────────────────────────────────────────┤
│ Danh sách hàng hóa                                         │
│ ┌────────┬──────────────────┬────────┬────────┬─────────┐ │
│ │ Mã HH  │ Tên hàng         │ Đơn giá│ Số lượng│ Loại   │ │
│ ├────────┼──────────────────┼────────┼────────┼─────────┤ │
│ │ MH001  │ Bút bi Thiên Long│  5000.0│     200│ VPP    │ │
│ │ MH002  │ Sữa tươi Vinamilk│ 32000.0│      50│ TP     │ │
│ └────────┴──────────────────┴────────┴────────┴─────────┘ │
└────────────────────────────────────────────────────────────┘
```

---

## 6. Xử lý ngoại lệ & tài nguyên

| Tình huống                         | Xử lý                                           |
|------------------------------------|-------------------------------------------------|
| Port đã bị sử dụng                 | `new ServerSocket(port)` ném `IOException` → hộp thoại báo lỗi ở Server. |
| Server chưa chạy khi Client kết nối| `new Socket(...)` ném `ConnectException` → Client hiện thông báo "Không kết nối được Server". |
| Client đột ngột đóng cửa sổ        | Thread handler bắt `SocketException` → log và đóng kết nối. |
| Stop Server                        | `serverSocket.close()` khiến `accept()` ném `SocketException`, vòng lặp thoát. |
| Đóng stream / socket               | Dùng `try-with-resources` (`ClientHandler`) và `close()` có try/catch (`SocketClient`). |
| Nhập dữ liệu sai phía Client       | `docFormValidate()` kiểm tra trước khi gửi.     |
| Gọi Server khi mất kết nối         | `SocketClient.send()` ném `IOException`, Client bắt và hiện thông báo. |

---

## 7. Sơ đồ luồng tổng thể

```
          ┌────────────┐               ┌──────────────────────┐
          │  Client 1  │               │        Server        │
          └─────┬──────┘               └──────────┬───────────┘
                │  new Socket(host,9999)          │
                │───── SYN / SYN+ACK / ACK ──────▶│ accept()
                │                                 │ → new ClientHandler (Thread)
                │  Request("VIEW")                │
                │──────── object bytes ──────────▶│ readObject
                │                                 │ service.xemTatCa()  (synchronized)
                │                                 │ writeObject(Response)
                │◀────────── object bytes ────────│
                │  Request("ADD", hangHoa)        │
                │──────── object bytes ──────────▶│ service.them(hh)
                │◀────── Response(success) ───────│
                │         ...                     │
                │  Request("EXIT")                │
                │─────────────────────────────────▶│ đóng socket, thoát thread
                │                                 │
          ┌─────┴──────┐               ┌──────────┴───────────┐
          │  Client 2  │ ──── kết nối song song ────▶ Thread 2
          └────────────┘               └──────────────────────┘
```

---

## 8. Danh sách file

```
ServerApp/
├── build.xml
├── manifest.mf
├── nbproject/
│   ├── project.xml
│   └── project.properties
└── src/
    ├── common/
    │   ├── HangHoa.java
    │   ├── Request.java
    │   └── Response.java
    └── serverapp/
        ├── ServerMain.java
        ├── ServerFrame.java
        ├── ClientHandler.java
        └── HangHoaService.java

ClientApp/
├── build.xml
├── manifest.mf
├── nbproject/
│   ├── project.xml
│   └── project.properties
└── src/
    ├── common/
    │   ├── HangHoa.java
    │   ├── Request.java
    │   └── Response.java
    └── clientapp/
        ├── ClientMain.java
        ├── ClientFrame.java
        └── SocketClient.java
```
