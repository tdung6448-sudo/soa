package serverapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Cấu hình kết nối MySQL (XAMPP) và cung cấp Connection.
 *
 * MẶC ĐỊNH cho XAMPP:
 *   host = localhost, port = 3306, user = root, password = rỗng
 *
 * Muốn đổi: sửa các hằng số bên dưới.
 * Driver JDBC: com.mysql.cj.jdbc.Driver  (mysql-connector-j-8.x.x.jar)
 */
public class DBConfig {

    public static final String HOST     = "localhost";
    public static final int    PORT     = 3306;
    public static final String DATABASE = "qlhanghoa";
    public static final String USER     = "root";
    public static final String PASSWORD = "";

    // useSSL=false + serverTimezone để tránh warning với Connector/J 8
    public static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useUnicode=true&characterEncoding=UTF-8"
            + "&useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=Asia/Ho_Chi_Minh";

    static {
        try {
            // Nạp driver. Với JDBC 4+ dòng này không bắt buộc
            // nhưng giữ để hiện lỗi rõ nếu thiếu thư viện.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(
                "Không tìm thấy MySQL JDBC Driver. "
                + "Hãy add mysql-connector-j-8.x.x.jar vào Libraries của project.",
                ex);
        }
    }

    /** Mở một Connection mới tới MySQL. Caller tự đóng. */
    public static Connection open() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private DBConfig() {}
}
