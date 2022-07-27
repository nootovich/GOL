import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Window extends JFrame {
    MainView mainView;

    Window() {
        mainView = new MainView();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(mainView);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static class MainView extends JPanel {
        private final int width = 400;
        private final int height = 400;
        private final int ww = width / 2;
        private final int hh = height / 2;
        public Point prevPos = new Point(-1, -1);
        public Point viewPos = new Point(0, 0);
        public Point prevViewPos = new Point(-1, -1);
        public int scale = 1;
        private BufferedImage pixels = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        MainView() {
            setPreferredSize(new Dimension(width, height));
            setVisible(true);
            setBounds(0, 0, width, height);
            Graphics2D p = pixels.createGraphics();
            p.setColor(Color.DARK_GRAY);
            p.fillRect(0, 0, width, height);
        }

        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.WHITE);
//            int x = width - (view.x / scale);
//            int y = ;

            g2d.drawImage(
                    pixels,
                    ww - scale * (ww - viewPos.x),
                    hh - scale * (hh - viewPos.y),
//                    width / 2 - width / 2 * scale + viewPos.x * scale,
//                    height / 2 - height / 2 * scale + viewPos.y * scale,
//                    a - ab + cb = a(1 - b) + cb = a - b(a + c) = a(1 - b + c/a) : Hmmmmm...
//                    a - b(a - c) - c * (b - 1) = a - ab + bc - bc + c = a - ab + c = a(1 - b) + c;
                    width * scale,
                    height * scale,
                    this
            );
            g2d.drawString(Arrays.toString(GameOfLife.mouseKeys), 20, 20);
        }

        public void drawPixel() {
            Point pos = getMousePosition();
            pos = new Point(
                    (pos.x - ww) / scale + ww - viewPos.x,
                    (pos.y - hh) / scale + hh - viewPos.y)
            ;
            if (prevPos.x == -1) prevPos = pos;
            if (!(pos.x < 0 || pos.y < 0 || pos.x >= width || pos.y >= height))
                pixels.createGraphics().drawLine(pos.x, pos.y, prevPos.x, prevPos.y);
//                        (pos.x, pos.y, Color.WHITE.getRGB());
            prevPos = pos;
        }

        public void moveView() {
            Point pos = getMousePosition();
            if (prevViewPos.x == -1) prevViewPos = pos;
            if (!(pos.x < 0 || pos.y < 0 || pos.x >= width || pos.y >= height))
                viewPos.setLocation(
                        viewPos.x + (pos.x - prevViewPos.x) / scale,
                        viewPos.y + (pos.y - prevViewPos.y) / scale
                );
            prevViewPos = pos;
            System.out.println(viewPos);
        }

        public void changeScale(int wheelRotation) {
            scale -= wheelRotation;
            if (scale <= 0) scale = 1;

//            if (wheelRotation < 0) {
//                System.out.println(
//                        scale + ":" + viewPos.x + ":" + viewPos.y + "\n"
//                                + width + ":" + height + "\n"
//                                + (viewPos.x - width / scale) + ":" + (viewPos.y - height / scale) + "\n"
//                                + (viewPos.x + (viewPos.x - width / scale)) + ":"
//                                + (viewPos.y + (viewPos.y - height / scale))
//                );
//                viewPos.setLocation(
//                        (viewPos.x + (viewPos.x - width / scale)),
//                        (viewPos.y + (viewPos.y - height / scale))
//                );
//            }
//            if (wheelRotation > 0) {
//                System.out.println(
//                        scale + ":" + viewPos.x + ":" + viewPos.y + "\n"
//                                + width + ":" + height + "\n"
//                                + (viewPos.x - width / scale) + ":" + (viewPos.y - height / scale) + "\n"
//                                + (viewPos.x + (viewPos.x + width / scale)) + ":"
//                                + (viewPos.y + (viewPos.y + height / scale))
//                );
//
//                viewPos.setLocation(
//                        (viewPos.x + (viewPos.x + width / scale)),
//                        (viewPos.y + (viewPos.y + height / scale))
//                );
//            }
        }
    }
}
