import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {
    public static Point mouse = new Point(0, 0);
    MainView mainView;

    Window() {
        mainView = new MainView();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(mainView);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    //        private static class CheckBox extends JPanel {
//            int x,y;
//            String text;
//            CheckBox(int x, int y, String text) {
//                this.x = x;
//                this.y = y;
//                this.text = text;
//
//            }
//        }
    private static boolean isInside(int value, int lower_bound, int upper_bound) {
        return value >= lower_bound && value <= upper_bound;
    }

    public static class MainView extends JPanel {
        private final int width = 1000;
        private final int height = 800;
        private final double ww = width / 2d;
        private final double hh = height / 2d;
        public Point prevPos = new Point(-1, -1);
        //        public Point viewPos = new Point(0,0);
//        public Point prevViewPos = new Point(-1, -1);
        public double viewPosX = 0d;
        public double viewPosY = 0d;
        public double prevViewPosX = -1d;
        public double prevViewPosY = -1d;
        public int scale = 1;
        ControlPanel panel;

        MainView() {
            panel = new ControlPanel(width / 4, height - 100, width / 2, 80);
            setPreferredSize(new Dimension(width, height));
            setVisible(true);
            setBounds(0, 0, width, height);
        }

        public void paint(Graphics g) {
            Point m = getMousePosition();
            try {
                mouse.setLocation(0d + m.x, 0d + m.y);
            } catch (NullPointerException ignored) {

            }
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.WHITE);
            g2d.drawImage(
                    GameOfLife.drawCells(g2d),
//                    pixels,
                    (int) (ww - scale * (ww - viewPosX)),
                    (int) (hh - scale * (hh - viewPosY)),
//                    width / 2 - width / 2 * scale + viewPos.x * scale,
//                    height / 2 - height / 2 * scale + viewPos.y * scale,
//                    a - ab + cb = a(1 - b) + cb = a - b(a + c) = a(1 - b + c/a) : Hmmmmm...
//                    a - b(a - c) - c * (b - 1) = a - ab + bc - bc + c = a - ab + c = a(1 - b) + c;
                    width * scale,
                    height * scale,
                    this
            );
            g2d.drawString("" + GameOfLife.paused, 20, 20);
            panel.update(g2d);
        }

        public void drawPixel() {
            if (panel.mouseInside()) return;
            mouse = new Point(
                    (int) ((mouse.x - ww) / scale + ww - viewPosX),
                    (int) ((mouse.y - hh) / scale + hh - viewPosY))
            ;
            if (prevPos.x == -1) prevPos.setLocation(0d + mouse.x, 0d + mouse.y);
            if (!(mouse.x < 0 || mouse.y < 0 || mouse.x >= width || mouse.y >= height)) {
                //pixels.createGraphics().drawLine(mouse.x, mouse.y, prevPos.x, prevPos.y);
                GameOfLife.cells[mouse.y][mouse.x] = true;
            }
            prevPos.setLocation(0d + mouse.x, 0d + mouse.y);
        }

        public void moveView() {
            if (prevViewPosY == -1d) setPrevViewPos(0d + mouse.x, 0d + mouse.y);
            if (!(mouse.x < 0 || mouse.y < 0 || mouse.x >= width || mouse.y >= height))
                setViewPos(
                        viewPosX + (0d + mouse.x - prevViewPosX) / scale,
                        viewPosY + (0d + mouse.y - prevViewPosY) / scale
                );
            setPrevViewPos(0d + mouse.x, 0d + mouse.y);
        }

        public void changeScale(int wheelRotation) {
            scale -= wheelRotation;
            if (scale <= 0) scale = 1;
        }

        public void setViewPos(double x, double y) {
            viewPosX = x;
            viewPosY = y;
        }

        public void setPrevViewPos(double x, double y) {
            prevViewPosX = x;
            prevViewPosY = y;
        }
    }

// TODO: add checkboxes to menu bar

    public static class ControlPanel extends JPanel {
        static Color idleColor = new Color(69, 69, 69, 200);
        static Color activeColor = new Color(42, 42, 42, 240);
        static Color c = idleColor;
        static Button b;
        int x, y, width, height;

        //           CheckBox checkboxes;
        //            Text text;
        ControlPanel(int x, int y, int width, int height) {
            b = new Button(x + 180, y + 20, 40, 40, "Toggle");
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            setPreferredSize(new Dimension(width, height));
            setVisible(true);
        }

        public void update(Graphics2D g2d) {
            if (mouseInside()) c = activeColor;
            else c = idleColor;
            draw(g2d);
            b.update(g2d);
        }

        public void draw(Graphics2D g2d) {
            g2d.setColor(c);
            g2d.fillRect(x, y, width, height);
        }

        public boolean mouseInside() {
            return (
                    mouse != null &&
                            isInside(mouse.x, x, x + width) &&
                            isInside(mouse.y, y, y + height)
            );
        }

        public static class Button {
            int x, y, width, height;
            String text;

            Button(int x, int y, int width, int height, String text) {
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
                this.text = text;
            }

            public void update(Graphics2D g2d) {
                if (mouse != null && GameOfLife.mouseKeys[0] && !GameOfLife.pauseLock &&
                        isInside(mouse.x, x, x + width) &&
                        isInside(mouse.y, y, y + height)
                ) GameOfLife.togglePause();
                draw(g2d);
            }

            public void draw(Graphics2D g2d) {
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillRect(x, y, width, height);
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, x, y + height / 2);
            }
        }
    }
}
