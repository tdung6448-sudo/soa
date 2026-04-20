package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Đối tượng phản hồi (response) Server gửi lại cho Client.
 *
 * - success: true nếu xử lý thành công
 * - message: thông điệp mô tả kết quả
 * - data: danh sách hàng hóa trả về (dùng cho VIEW / SEARCH)
 */
public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private List<HangHoa> data;

    public Response() {
        this.data = new ArrayList<>();
    }

    public Response(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = new ArrayList<>();
    }

    public Response(boolean success, String message, List<HangHoa> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<HangHoa> getData() { return data; }
    public void setData(List<HangHoa> data) { this.data = data; }
}
