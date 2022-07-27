import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Window extends JFrame {
    MainView mainView;

    Window() {
        mainView = new MainView();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(mainView);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static class MainView extends JPanel {
        private final int width = 400;
        private final int height = 400;

        MainView() {
            this.setPreferredSize(new Dimension(width, height));
            this.setVisible(true);
        }

        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.WHITE);
            g2d.drawString(Arrays.toString(GameOfLife.mouseKeys), 20, 20);
        }
    }
}
