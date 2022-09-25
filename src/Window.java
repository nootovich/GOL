import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Window extends JFrame {
    public static boolean keyLock = false;
    public static int curCellDrawType = -1;
    public static char prevKey = '\'';
    public static Point mouse = new Point();
    static MainView mainView;

    ComponentAdapter componentAdapter = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            super.componentResized(e);
            changeSize(e.getComponent().getSize());
        }
    };
    MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == 1) GameOfLife.mouseKeys[0] = true;
            if (e.getButton() == 3) GameOfLife.mouseKeys[1] = true;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == 1) {
                GameOfLife.mouseKeys[0] = false;
                curCellDrawType = -1;
            }
            if (e.getButton() == 3) {
                GameOfLife.mouseKeys[1] = false;
                mainView.setPrevViewPos(-1, -1);
            }

        }

        @Override
        public void mouseExited(MouseEvent e) {
            GameOfLife.mouseKeys[0] = false;
            GameOfLife.mouseKeys[1] = false;
        }
    };
    KeyAdapter keyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            char key = e.getKeyChar();
            switch (key) {
                case 'm' -> mainView.scrollLock = !mainView.scrollLock;
                case '=', '+' -> mainView.changeScale(1);
                case '-' -> mainView.changeScale(-1);
                case '[' -> GameOfLife.changeSimStepTime(false);
                case ']' -> GameOfLife.changeSimStepTime(true);
                case 'w' -> GameOfLife.keys[0] = true;
                case 'a' -> GameOfLife.keys[1] = true;
                case 's' -> GameOfLife.keys[2] = true;
                case 'd' -> GameOfLife.keys[3] = true;
                case 'r' -> GameOfLife.randomFill();
                case '\n' -> GameOfLife.simulate();
                case ' ' -> {
                    GameOfLife.paused = !GameOfLife.paused;
                    GameOfLife.prevSimStep = 0;
                }
                case 'q' -> System.exit(1);
            }
            prevKey = key;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyChar()) {
                case 'w' -> GameOfLife.keys[0] = false;
                case 'a' -> GameOfLife.keys[1] = false;
                case 's' -> GameOfLife.keys[2] = false;
                case 'd' -> GameOfLife.keys[3] = false;
            }
            prevKey = '\'';
            keyLock = false;
        }
    };

    Window(int width, int height) {
        mainView = new MainView(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(mainView);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        addComponentListener(componentAdapter);
        addMouseListener(mouseAdapter);
        addKeyListener(keyAdapter);
        addMouseWheelListener(e -> mainView.changeScale(-e.getWheelRotation()));
    }

    public void changeSize(Dimension in) {
        Dimension out = new Dimension(Math.max(in.width, 750), Math.max(in.height, 300));
        setSize(out.width, out.height);
        mainView.changeSize(out);
    }

    public static class MainView extends JPanel {
        public boolean scrollLock = true;
        public int width, height;
        public double ww, hh, viewPosX, viewPosY, prevViewPosX = -1d, prevViewPosY = -1d, scale = 1d;
        ControlPanel panel;

        MainView(int width, int height) {
            this.width = width;
            this.height = height;
            this.ww = width / 2d;
            this.hh = height / 2d;
            viewPosX = (width - GameOfLife.cellsWidth) / 2d;
            viewPosY = (height - GameOfLife.cellsHeight) / 2d;
            panel = new ControlPanel(width / 4, height - 100, (int) ww, 80);
            setPreferredSize(new Dimension(width, height));
            setVisible(true);
            setBounds(0, 0, width, height);
        }

        public static BufferedImage drawCells() {
            int h = GameOfLife.cells.length, w = GameOfLife.cells[0].length;
            BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D temp = result.createGraphics();
            temp.setColor(GameOfLife.paused ? new Color(42, 42, 47) : Color.DARK_GRAY);
            temp.fillRect(0, 0, w, h);
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    if (GameOfLife.cells[i][j]) result.setRGB(j, i, Color.WHITE.getRGB());
                }
            }
            return result;
        }

        public static int sum(ArrayList<Long> timings) {
            int result = 0;
            if (timings.size() > 0) for (Long i : timings) if (i != null) result += i;
            return result > 0 ? result : 1;
        }

        public void changeSize(Dimension d) {
            width = d.width - 10;
            height = d.height - 36;
            ww = width / 2d;
            hh = height / 2d;
            viewPosX = (width - GameOfLife.cellsWidth) / 2d;
            viewPosY = (height - GameOfLife.cellsHeight) / 2d;
            panel.move(width, (int) ww, height, (int) hh);
        }

        public void paint(Graphics g) {

            // Get mouse position
            Point mousePos = getMousePosition();
            try {
                mouse.setLocation(0d + mousePos.x, 0d + mousePos.y);
            } catch (NullPointerException ignored) {
            }

            // Draw graphics
            updateView();
            Graphics2D g2d = (Graphics2D) g;

            // BG
            g2d.setColor(new Color(31, 31, 31));
            g2d.fillRect(0, 0, width, height);

            // CELLS
            g2d.drawImage(drawCells(), (int) (ww - scale * (ww - viewPosX)), (int) (hh - scale * (hh - viewPosY)), (int) (GameOfLife.cellsWidth * scale), (int) (GameOfLife.cellsHeight * scale), this);

            // GRID (TURNED OFF BECAUSE BROKEN)
            // if (scale >= 8) drawGrid(g2d);

            // PANEL
            panel.update(g2d);

            // TEXT
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, 35, 20); // FPS BG
            g2d.fillRect(10, height - 150, 175, 145); // CONTROLS BG
            g2d.setColor(Color.WHITE);
            g2d.drawString((int) (((mouse.x - ww) / scale + ww - viewPosX)) + " : " + (int) (((mouse.y - hh) / scale + hh - viewPosY)), panel.x + panel.width - 75, panel.y + 20);

            // TODO: Fix fps counter
            g2d.drawString((long) (1_000_000_000d / ((double) sum(GameOfLife.timings) / GameOfLife.frameSamples) + 0.5d) + "", 5, 15);

            // CONTROLS
            g2d.drawString("Controls:", 15, height - 135);
            g2d.drawString("WASD - Look around", 15, height - 115);
            g2d.drawString("Space - " + (GameOfLife.paused ? "Play" : "Pause"), 15, height - 100);
            g2d.drawString("Enter - Step forward", 15, height - 85);
            g2d.drawString("+/- - Change scale", 15, height - 70);
            g2d.drawString("[/] - Change speed", 15, height - 55);
            g2d.drawString("R - Random fill", 15, height - 40);
            g2d.drawString("M - Edge screen scroll " + (scrollLock ? "ON" : "OFF"), 15, height - 25);
            g2d.drawString("Q - Quit", 15, height - 10);
        }

        public void addCell() {
            if (panel.isSelected()) return;
            double mx = mouse.x, my = mouse.y, vx = viewPosX, vy = viewPosY, cw = GameOfLife.cellsWidth, ch = GameOfLife.cellsHeight;
            Point curCell = new Point((int) (((mx - ww) / scale + ww - vx)), (int) (((my - hh) / scale + hh - vy)));
            if (!(curCell.x < 0 || curCell.y < 0 || curCell.x >= cw || curCell.y >= ch)) {
                if (curCellDrawType == -1) curCellDrawType = GameOfLife.cells[curCell.y][curCell.x] ? 1 : 0;
                GameOfLife.cells[curCell.y][curCell.x] = curCellDrawType == 0;
            }
        }

        private void drawGrid(Graphics2D g2d) { // TODO: Fix offsets (DISABLED)
            g2d.setColor(Color.DARK_GRAY);
            double x_offset = (width + viewPosX) % 1;
            double y_offset = (height + viewPosY) % 1;
            for (int i = -1; i < height / scale; i++) {
                int y = (int) ((y_offset + i) * scale);
                g2d.drawLine(0, y, width, y);
                g2d.drawString(y + "", width - 20, y);
            }
            for (int j = -1; j < width / scale; j++) {
                int x = (int) ((x_offset + j) * scale);
                g2d.drawLine(x, 0, x, height);
                g2d.drawString(x + "", x, 50);
            }
            g2d.setColor(Color.WHITE);
            g2d.drawString("x/y offsets = " + x_offset + ":" + y_offset, 20, 100);
            g2d.drawString("viewPos = " + viewPosX + ":" + viewPosY, 20, 120);
            g2d.drawString("vP/cellsDrawX = " + (width - GameOfLife.cellsWidth) / 2d + ":" + (ww - scale * (ww - viewPosX)), 20, 140);
        }

        public void moveViewByMouse() {
            if (prevViewPosY == -1d) setPrevViewPos(mouse.x, mouse.y);
            if (!(mouse.x < 0 || mouse.y < 0 || mouse.x >= width || mouse.y >= height))
                setViewPos(viewPosX + (0d + mouse.x - prevViewPosX) / scale, viewPosY + (0d + mouse.y - prevViewPosY) / scale);
            setPrevViewPos(mouse.x, mouse.y);
        }

        public void updateView() {
            double offsetX = 0, offsetY = 0;
            if (GameOfLife.keys[0] || (mouse.y < height / 32 && !scrollLock)) offsetY = 1;
            if (GameOfLife.keys[1] || (mouse.x < width / 32 && !scrollLock)) offsetX = 1;
            if (GameOfLife.keys[2] || (mouse.y > height - (height / 32) && !scrollLock)) offsetY = -1;
            if (GameOfLife.keys[3] || (mouse.x > width - (width / 32) && !scrollLock)) offsetX = -1;
            if (GameOfLife.mouseKeys[0]) addCell();
            if (GameOfLife.mouseKeys[1]) moveViewByMouse();
            double mlt = 1000d / scale / GameOfLife.FPS;
            setViewPos(viewPosX + (mlt * offsetX), viewPosY + (mlt * offsetY));
        }

        public void changeScale(int direction) {
            double step = Math.pow(2, direction);
            scale *= step;
            if (scale <= 0.2d) scale = 0.25d;
            if (scale > 32) scale = 32d;
        }

        public void setViewPos(double x, double y) {
            viewPosX = x;
            viewPosY = y;
        }

        public void setPrevViewPos(int x, int y) {
            prevViewPosX = 0d + x;
            prevViewPosY = 0d + y;
        }
    }

    public static class ControlPanel extends Controls {
        static Controls[] controls;
        static Button togglePause;
        static Button stepForward;
        static Button randomFill;
        static Button changeSize;
        static TextField cellsWidth;
        static TextField cellsHeight;
        // static CheckBox test;
        static Slider Simspeed;

        ControlPanel(int x, int y, int width, int height) {
            super(x, y, width, height, true, "", -1);
            idleColor = new Color(23, 26, 29, 180);
            selectedColor = new Color(37, 43, 49, 220);
            outlineColor = Color.BLACK;
            selectedOutlineColor = Color.DARK_GRAY;
            togglePause = new Button(x + width / 2 - 20, y + 10, 40, 40, "", 0);
            stepForward = new Button(x + width / 2 + 20, y + 10, 40, 40, "Next\nStep", 1);
            randomFill = new Button(x + width / 2 - 60, y + 10, 40, 40, "Rand", 2);
            changeSize = new Button(x + width / 2 + 70, y + 35, 80, 15, "Change size", 3);
            cellsWidth = new TextField(x + width / 2 + 70, y + 10, 35, 20, GameOfLife.cellsWidth, 0);
            cellsHeight = new TextField(x + width / 2 + 115, y + 10, 35, 20, GameOfLife.cellsHeight, 1);
            Simspeed = new Slider(x + 20, y + height - 20, width - 40, 1, 1000, GameOfLife.simulationStepsPerSecond, "Simspeed", -1);
//          test = new CheckBox(x + 20, y + 10, 20, "Test checkbox", -1);
            controls = new Controls[]{togglePause, stepForward, randomFill, changeSize, cellsWidth, cellsHeight, Simspeed};
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

        @Override
        public void move(int w, int ww, int h, int hh) {
            x = w / 4;
            width = ww;
            y = h - 100;
            for (Controls c : controls) {
                c.move(x, y, width, height);
            }
        }

        private static class Button extends Controls {
            Button(int x, int y, int width, int height, String text, int id) {
                super(x, y, width, height, false, text, id);
            }

            @Override
            public void move(int offsetX, int offsetY, int parentWidth, int parentHeight) {
                y = offsetY + 10;
                x = offsetX + parentWidth / 2;
                switch (id) {
                    case 0 -> x -= 20;
                    case 1 -> x += 20;
                    case 2 -> x -= 60;
                    case 3 -> {
                        y += 25;
                        x += 70;
                    }
                }
            }

            @Override
            public void onActive() {
                if (id == 0) {
                    GameOfLife.paused = !GameOfLife.paused;
                    GameOfLife.prevSimStep = 0;
                } else if (id == 1) GameOfLife.simulate();
                else if (id == 2) GameOfLife.randomFill();
                else if (id == 3) {
                    int width = cellsWidth.getValue(), height = cellsHeight.getValue();
                    GameOfLife.cellsWidth = width;
                    GameOfLife.cellsHeight = height;
                    GameOfLife.paused = true;
                    GameOfLife.needToChangeSize = true;
                    mainView.setViewPos((mainView.width - width) / 2d, (mainView.height - height) / 2d);
                }
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
                } else if (id == 3) {
                    g2d.drawString("Change", x + 14, (int) (y + height * 0.8));
                }
            }
        }

        private static class CheckBox extends Controls {
            boolean toggle = false;
            static int offset = 4;

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
                this.pos = map(defVal, minVal, maxVal, x, x + width);
                size = height;
            }

            @Override
            public void move(int offsetX, int offsetY, int parentWidth, int parentHeight) {
                x = offsetX + 20;
                y = offsetY + parentHeight - 20;
                width = parentWidth - 40;
                updatePos();
            }

            public double map(double val, double in_low, double in_high, double out_low, double out_high) {
                double slope = (out_high - out_low) / (in_high - in_low);
                return (out_low + slope * (val - in_low));
            }

            public void changePos(int val) {
                pos = val;
                if (pos < x) pos = x;
                if (pos > x + width) pos = x + width;
                GameOfLife.simulationStepsPerSecond = (int) (changeVal() + 0.5d);
            }

            public void updatePos() {
                pos = map(GameOfLife.simulationStepsPerSecond, minVal, maxVal, x, x + width);
                if (pos < x) pos = x;
                if (pos > x + width) pos = x + width;
                changeVal();
            }

            public double changeVal() {
                curVal = map(pos, x, x + width, minVal, maxVal);
                return curVal;
            }

            @Override
            public void onActive() {
                changePos(mouse.x);
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
                g2d.drawString(text + " : " + (int) (curVal + 0.5d), x, y - size / 2);
            }
        }

        public static class TextField extends Controls {
            boolean lockActiveState = false;
            int value, prevValue;

            TextField(int x, int y, int width, int height, int defVal, int id) {
                super(x, y, width, height, false, "", id);
                idleColor = Color.LIGHT_GRAY;
                selectedColor = Color.WHITE;
                outlineColor = Color.DARK_GRAY;
                selectedOutlineColor = Color.GRAY;
                this.value = defVal;
                this.prevValue = defVal;
            }

            public int getValue() {
                return value;
            }

            @Override
            public void onActive() {
                lockActiveState = !lockActiveState;
                prevValue = value;
            }

            @Override
            public void postUpdate(Graphics2D g2d) {
                if (lockActiveState) {
                    if (!isSelected() && GameOfLife.mouseKeys[0]) {
                        lockActiveState = false;
                        if (value == 0) value = prevValue;
                    }
                    selected = true;
                    draw(g2d);
                    int key = prevKey - 48;
                    if (!keyLock && key >= 0 && key < 10) {
                        if (value == prevValue) value = 0;
                        value *= 10;
                        value += key;
                        if (value > 999) value = 999;
                        if (value < 1) value = 1;
                        keyLock = true;
                    }
                }
            }

            @Override
            public void postDraw(Graphics2D g2d) {
                if (lockActiveState && (int) (GameOfLife.prevFrame / 500_000_000d) % 2 > 0) {
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect((int) (x + 5 + Math.log10(value)), y + 4, 0, 12);
                }
                g2d.setColor(Color.BLACK);
                g2d.drawString(value + "", x + 4, y + height - 5);
            }
        }

    }

    private static class Controls {
        boolean selected, active, mouseLock, type;
        int x, y, width, height, size, id;
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
            boolean lmb = GameOfLife.mouseKeys[0], result = false;
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

        public void move(int offsetX, int offsetY, int windowWidth, int windowHeight) {

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
