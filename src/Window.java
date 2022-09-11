import javax.swing.*;
import java.awt.*;

// GLOBAL TODO: Make window, mainView and cells independent in size. Make window resizeable. Make controls attached to bottom of window and potentially movable.
public class Window extends JFrame {
    public static Point mouse = new Point();
    MainView mainView;

    Window(int width, int height) {
        mainView = new MainView(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(mainView);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static class MainView extends JPanel {
        private final int width, height;
        private final double ww, hh;
        public double viewPosX, viewPosY, prevViewPosX = -1d, prevViewPosY = -1d, scale = 1d;
        ControlPanel panel;

        MainView(int width, int height) {
            this.width = width;
            this.height = height;
            this.ww = width / 2d;
            this.hh = height / 2d;
            panel = new ControlPanel(width / 4, height - 100, (int) ww, 80);
            setPreferredSize(new Dimension(width, height));
            setVisible(true);
            setBounds(0, 0, width, height);
        }

        public void paint(Graphics g) {
            // Get mouse position
            Point mousePos = getMousePosition();
            try {
                mouse.setLocation(0d + mousePos.x, 0d + mousePos.y); // TODO: try to remove 0d
            } catch (NullPointerException ignored) {
            }

            // Draw graphics
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(31, 31, 31));
            g2d.fillRect(0, 0, width, height);
            g2d.drawImage(GameOfLife.drawCells(), (int) (ww - scale * (ww - viewPosX)), (int) (hh - scale * (hh - viewPosY)), (int) (width * scale), (int) (height * scale), this);
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 5, 80, 20);
            g2d.fillRect(0, 45, 1000, 20);
            g2d.fillRect(0, 85, 60, 20);
            g2d.setColor(Color.WHITE);
            g2d.drawString(
                    (int) (((mouse.x - ww) / scale + ww - viewPosX) / (GameOfLife.windowWidth / GameOfLife.cellsWidth)) + " : " +
                            (int) (((mouse.y - hh) / scale + hh - viewPosY) /
                                    (GameOfLife.windowHeight / GameOfLife.cellsHeight)), 20, 20);
            g2d.drawString(GameOfLife.timings.toString(), 20, 60);
            g2d.drawString(1000 * GameOfLife.timings.size() / GameOfLife.sum(GameOfLife.timings) + "", 20, 100);

            panel.update(g2d);
        }

        public void addCell() {
            if (panel.isSelected()) return;

            double
                    mx = mouse.x, my = mouse.y,
                    vx = viewPosX, vy = viewPosY,
                    gww = GameOfLife.windowWidth, gwh = GameOfLife.windowHeight,
                    cw = GameOfLife.cellsWidth, ch = GameOfLife.cellsHeight;

            Point curCell = new Point(
                    (int) (((mx - ww) / scale + ww - vx) / (gww / cw)),
                    (int) (((my - hh) / scale + hh - vy) / (gwh / ch)));

            if (!(curCell.x < 0 || curCell.y < 0 || curCell.x >= cw || curCell.y >= ch)) {
                if (GameOfLife.curCellDrawType == -1)
                    GameOfLife.curCellDrawType = GameOfLife.cells[curCell.y][curCell.x] ? 1 : 0;
                GameOfLife.cells[curCell.y][curCell.x] = GameOfLife.curCellDrawType == 0;
            }
        }

        public void moveView() {
            if (prevViewPosY == -1d) setPrevViewPos(mouse.x, mouse.y);
            if (!(mouse.x < 0 || mouse.y < 0 || mouse.x >= width || mouse.y >= height))
                setViewPos(viewPosX + (0d + mouse.x - prevViewPosX) / scale, viewPosY + (0d + mouse.y - prevViewPosY) / scale); // TODO: try to remove 0d
            setPrevViewPos(mouse.x, mouse.y);
        }

        public void changeScale(int direction) {
            double step = Math.pow(2, direction);
            scale *= step;
            if (scale <= 0.2d) scale = 0.25d;
        }

        public void setViewPos(double x, double y) {
            viewPosX = x;
            viewPosY = y;
        }

        public void setPrevViewPos(int x, int y) { // TODO: try to remove 0d
            prevViewPosX = 0d + x;
            prevViewPosY = 0d + y;
        }
    }

    public static class ControlPanel extends Controls {
        Controls[] controls;
        Button togglePause;
        Button stepForward;
        Button randomFill;
        CheckBox test;
        Slider Simspeed;

        ControlPanel(int x, int y, int width, int height) {
            super(x, y, width, height, true, "", -1);
            idleColor = new Color(23, 26, 29, 180);
            selectedColor = new Color(37, 43, 49, 220);
            outlineColor = Color.BLACK;
            selectedOutlineColor = Color.DARK_GRAY;
            this.togglePause = new Button(
                    x + width / 2 - 20, y + 10, 40, 40, "", 0);
            this.stepForward = new Button(
                    x + width / 2 + 20, y + 10, 40, 40, "Next\nStep", 1);
            this.randomFill = new Button(
                    x + width / 2 - 60, y + 10, 40, 40, "Rand", 2);
            this.test = new CheckBox(
                    x + 20, y + 10, 20, "Test checkbox", -1);
            this.Simspeed = new Slider(
                    x + 20, y + height - 20, width - 40, 1, 1000, GameOfLife.simulationStepsPerSecond, "Simspeed", -1);
            controls = new Controls[]{togglePause, stepForward, randomFill, test, Simspeed};
        }

        @Override
        public void onActive() {
        }

        @Override
        public void postUpdate(Graphics2D g2d) {
            for (Controls c : controls) {
                c.update(g2d);
            }
        }

        private static class Button extends Controls {
            Button(int x, int y, int width, int height, String text, int id) {
                super(x, y, width, height, false, text, id);
            }

            @Override
            public void onActive() {
                if (id == 0) {
                    GameOfLife.paused = !GameOfLife.paused;
                    GameOfLife.prevSimStep = 0;
                } else if (id == 1) GameOfLife.simulate();
                else if (id == 2) GameOfLife.randomFill();
            }

            @Override
            public void postDraw(Graphics2D g2d) {
                g2d.setColor(Color.WHITE);
                if (id == 0) {
                    String textToDraw = GameOfLife.paused ? "Play" : "Pause";
                    int textOffset = GameOfLife.paused ? 8 : 2;
                    g2d.drawString(textToDraw, x + textOffset, (int) (y + height * 0.6));
                } else if (id == 1) {
                    g2d.drawString("Next", x + 8, (int) (y + height * 0.4));
                    g2d.drawString("Step", x + 8, (int) (y + height * 0.8));
                } else if (id == 2) {
                    g2d.drawString("Rand", x + 4, (int) (y + height * 0.6));
                }
            }
        }

        private static class CheckBox extends Controls {
            static int offset = 4;
            boolean toggle = false;

            CheckBox(int x, int y, int size, String text, int id) {
                super(x, y, size, false, text, id);
                idleColor = Color.LIGHT_GRAY;
            }

            @Override
            public void onActive() {
                toggle = !toggle;
            }

            @Override
            public void postDraw(Graphics2D g2d) {
                if (toggle) {
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(x + offset, y + offset, width - offset * 2, height - offset * 2);
                }
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, x + width + 5, (int) (y + height * 0.7));
            }
        }

        public static class Slider extends Controls {
            double minVal, maxVal, curVal, pos;

            Slider(int x, int y, int width, double minVal, double maxVal, double defVal, String text, int id) {
                super(x, y, width, 16, true, text, id);
                this.minVal = minVal;
                this.maxVal = maxVal;
                this.curVal = defVal;
                this.pos = GameOfLife.map(defVal, minVal, maxVal, x, x + width);
                size = height;
            }

            public void changePos() {
                pos = mouse.x;
                if (pos < x) pos = x;
                if (pos > x + width) pos = x + width;
                changeVal();
            }

//            public void changePos(double offset) {
//                pos = GameOfLife.map(curVal + offset, minVal, maxVal, x, x + width);
//                if (pos < x) pos = x;
//                if (pos > x + width) pos = x + width;
//                System.out.println(offset +" : " + pos);
//                changeVal();
//            } // TODO: possibly remove this

            public void changeVal() {
                curVal = GameOfLife.map(pos, x, x + width, minVal, maxVal);
                GameOfLife.simulationStepTime = 1000 / curVal; // TODO: make so it changes the num of sim steps instead of the timing itself (same applies for keyboard handler in main file)
            }

            @Override
            public void onActive() {
                changePos();
            }

            @Override
            public void draw(Graphics2D g2d) {
                g2d.setColor(Color.RED);
                g2d.fillRect(x, y + size / 2 - 1, width, 2);
                if (selected) {
                    g2d.setColor(Color.LIGHT_GRAY);
                } else {
                    g2d.setColor(Color.GRAY);
                }
                g2d.fillRect((int) pos, y, size / 4, size);
                g2d.setColor(Color.WHITE);
                g2d.drawString(text + " : " + (int) (curVal), x, y - size / 2);
            }
        }

    }

    private static class Controls {
        int x, y, width, height, size, id;
        boolean selected, active, mouseLock, type;
        String text;
        Color idleColor = Color.DARK_GRAY, selectedColor = Color.GRAY, outlineColor = Color.GRAY, selectedOutlineColor = Color.LIGHT_GRAY;

        Controls(int x, int y, int width, int height, boolean type, String text, int id) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = type;
            this.text = text;
            this.id = id;
        }

        Controls(int x, int y, int size, boolean type, String text, int id) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.width = size;
            this.height = size;
            this.type = type;
            this.text = text;
            this.id = id;
        }

        public boolean isSelected() {
            return (mouse.x >= x && mouse.x <= x + width && mouse.y >= y && mouse.y <= y + height);
        }

        public boolean isActivated() {
            boolean lmb = GameOfLife.mouseKeys[0];
            boolean result = false;
            if (!type) {
                if (lmb && !mouseLock) result = true;
            } else {
                result = lmb;
            }
            mouseLock = lmb;
            return result;
        }

        public void onActive() {
            StringBuilder result = new StringBuilder();
            result.append(x).append(" ").append(y).append(" ").append(width).append(" ").append(height).append(" ").append(size).append(" ").append(type).append(" ").append(text);
            System.out.println(result);
        }

        public void update(Graphics2D g2d) {
            preUpdate(g2d);
            if (mouse != null) {
                selected = isSelected();
                if (selected) {
                    active = isActivated();
                    if (active) {
                        onActive();
                    }
                }
            }
            draw(g2d);
            postUpdate(g2d);
        }

        public void preUpdate(Graphics2D g2d) {
        }

        public void postUpdate(Graphics2D g2d) {
        }

        public void draw(Graphics2D g2d) {
            preDraw(g2d);
            g2d.setColor(idleColor);
            if (selected) g2d.setColor(selectedColor);
            g2d.fillRect(x, y, width, height);
            g2d.setColor(outlineColor);
            if (selected) g2d.setColor(selectedOutlineColor);
            g2d.drawRect(x, y, width, height);
            postDraw(g2d);
        }

        public void preDraw(Graphics2D g2d) {
        }

        public void postDraw(Graphics2D g2d) {
            g2d.setColor(Color.WHITE);
            g2d.drawString(text, x, y + height / 2);
        }
    }
}
