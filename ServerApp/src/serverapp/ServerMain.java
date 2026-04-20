package serverapp;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Điểm vào của ứng dụng Server.
 * Khởi tạo giao diện trên Event Dispatch Thread của Swing.
 */
public class ServerMain {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new ServerFrame().setVisible(true));
    }
}
