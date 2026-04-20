package clientapp;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Điểm vào của ứng dụng Client.
 * Khởi tạo giao diện trên Event Dispatch Thread.
 */
public class ClientMain {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new ClientFrame().setVisible(true));
    }
}
