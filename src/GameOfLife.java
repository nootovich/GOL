import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

// GLOBAL TODO: Add instructions for keybinds

public class GameOfLife {
    public static int /* SETTINGS ------------------------- */ // TODO: Put here most important variables and remove other stuff
            windowWidth = 960, windowHeight = 640, cellsWidth = 96, cellsHeight = 64, FPS = 100;
    public static double prevFrame = 0;
    public static long prevSimStep = 0;
    public static double frameTime = 1000d / FPS;
    public static boolean[][] cells = new boolean[cellsHeight][cellsWidth];
    public static int frameSamples = 40, simulationStepsPerSecond = 10, curCellDrawType = -1;
    public static double simulationStepTime = 1000d / simulationStepsPerSecond;
    public static boolean[] mouseKeys = new boolean[]{false, false};
    public static boolean paused = true;
    public static Window window = new Window(windowWidth, windowHeight);
    public static ArrayList<Integer> timings = new ArrayList<>();

    public static long prevMil = 0, prevNano = 0;

    public static void main(String[] args) {
        initMouse();
        initKeyboard();
        while (true) {
            long time = System.currentTimeMillis();
            double frameDiff = 0;
            if (prevFrame > 0) frameDiff = time - prevFrame - frameTime;
            if (frameDiff >= 0) {
                if (prevFrame > 0) timings.add((int) frameDiff);
                if (timings.size() > frameSamples) timings.remove(0);

                prevFrame = time - (frameDiff - frameTime);

                long temp = 0;
                if (prevSimStep > 1) temp = (long) (time - prevSimStep - simulationStepTime);
                if (temp >= 0 && !paused) {

//                    long curMil = System.currentTimeMillis();
//                    long curNano = System.nanoTime();
//                    System.out.println((prevMil-curMil)+"\n"+(prevNano-curNano));
//                    prevMil = curMil;
//                    prevNano = curNano; // TODO: fix framerate and simulation timing by using more precise timings

                    while (temp >= 0) {
                        prevSimStep += simulationStepTime;
                        temp -= simulationStepTime;
                        simulate();
                    }
                    prevSimStep = time;
                }
                if (mouseKeys[0]) window.mainView.addCell();
                if (mouseKeys[1]) window.mainView.moveView();
                window.repaint();
            }
        }
    }

    public static void randomFill() {
        int h = cells.length;
        int w = cells[0].length;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                cells[i][j] = Math.random() > 0.5d;
            }
        }
    }

    public static int sum(ArrayList<Integer> timings) {
        int result = 0;
        for (Integer i : timings) result += i;
        return result > 0 ? result : 1;
    }

    public static void simulate() {

        // DEFAULT RULES
        // If the cell is alive, then it is alive if it has 2 or 3 live neighbors
        // If the cell is dead , then it is alive if it has      3 live neighbors
        // TODO: Make menu for changing rules and rework how rules are treated (remove hardcoded stuff)

        int h = cells.length, w = cells[0].length;
        boolean[][] next = new boolean[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int neighbours = 0;
                if (i > 0) neighbours += cells[i - 1][j] ? 1 : 0;
                if (i > 0 && j < w - 1) neighbours += cells[i - 1][j + 1] ? 1 : 0;
                if (j > 0) neighbours += cells[i][j - 1] ? 1 : 0;
                if (i < h - 1 && j > 0) neighbours += cells[i + 1][j - 1] ? 1 : 0;
                if (i > 0 && j > 0) neighbours += cells[i - 1][j - 1] ? 1 : 0;
                if (i < h - 1) neighbours += cells[i + 1][j] ? 1 : 0;
                if (j < w - 1) neighbours += cells[i][j + 1] ? 1 : 0;
                if (i < h - 1 && j < w - 1) neighbours += cells[i + 1][j + 1] ? 1 : 0;
                if (cells[i][j] && (neighbours == 2 || neighbours == 3)) next[i][j] = true;
                else next[i][j] = neighbours == 3;
            }
        }
        cells = next;
    }

    public static BufferedImage drawCells() {
        int h = cells.length, w = cells[0].length;
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D temp = result.createGraphics();
        temp.setColor(paused ? new Color(42, 42, 47) : Color.DARK_GRAY);
        temp.fillRect(0, 0, w, h);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (cells[i][j]) result.setRGB(j, i, Color.WHITE.getRGB());
            }
        }
        return result;
    }

    private static void initMouse() {
        window.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 1) mouseKeys[0] = true;
                if (e.getButton() == 3) mouseKeys[1] = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == 1) {
                    mouseKeys[0] = false;
                    curCellDrawType = -1;
                }
                if (e.getButton() == 3) {
                    mouseKeys[1] = false;
                    window.mainView.setPrevViewPos(-1, -1);
                }

            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseKeys[0] = false;
                mouseKeys[1] = false;
            }
        });
        window.addMouseWheelListener(e -> window.mainView.changeScale(-e.getWheelRotation()));
//        window.addMouseWheelListener(new MouseWheelListener() {
//            @Override
//            public void mouseWheelMoved(MouseWheelEvent e) {
//                window.mainView.changeScale(-e.getWheelRotation());
//            }
//        });
    }

    private static void initKeyboard() {
        window.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyChar()) {
                    case '=' -> window.mainView.changeScale(1);
                    case '-' -> window.mainView.changeScale(-1);
                    case ' ' -> paused = !paused;
//                    case '[' ->  // TODO: same as slider; change to num of steps (and add this at all)
//                        window.mainView.panel.Simspeed.changePos(-10);
//                    case ']' -> window.mainView.panel.Simspeed.changePos(10);
                    case '\n' -> simulate();
                    case 'q' -> System.exit(1);
                    case 'r' -> randomFill();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }

    public static double map(double val, double in_low, double in_high, double out_low, double out_high) {
        double slope = (out_high - out_low) / (in_high - in_low);
        return (out_low + slope * (val - in_low));
    }
}