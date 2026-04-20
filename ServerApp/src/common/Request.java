package common;

import java.io.Serializable;

/**
 * Đối tượng chứa yêu cầu (request) Client gửi lên Server.
 *
 * - command: tên lệnh (ADD, VIEW, SEARCH, UPDATE, DELETE, EXIT)
 * - keyword: chuỗi tìm kiếm hoặc mã hàng cần thao tác
 * - hangHoa: đối tượng dữ liệu đi kèm (khi ADD/UPDATE)
 */
public class Request implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String CMD_ADD    = "ADD";
    public static final String CMD_VIEW   = "VIEW";
    public static final String CMD_SEARCH = "SEARCH";
    public static final String CMD_UPDATE = "UPDATE";
    public static final String CMD_DELETE = "DELETE";
    public static final String CMD_EXIT   = "EXIT";

    private String command;
    private String keyword;
    private HangHoa hangHoa;

    public Request() {}

    public Request(String command) { this.command = command; }

    public Request(String command, String keyword) {
        this.command = command;
        this.keyword = keyword;
    }

    public Request(String command, HangHoa hangHoa) {
        this.command = command;
        this.hangHoa = hangHoa;
    }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public HangHoa getHangHoa() { return hangHoa; }
    public void setHangHoa(HangHoa hangHoa) { this.hangHoa = hangHoa; }
}
