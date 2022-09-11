import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
//import java.util.ArrayList;

public class GameOfLife {
    //    public static ArrayList<Integer> alive = new ArrayList<>();
    public static Window window = new Window();
    public static long prevFrame = 0;
    public static long prevSimStep = 0;
    public static int FramesPerSecond = 50;
    public static int FrameTime = 1000 / FramesPerSecond;
    public static int simulationStepsPerSecond = 3;
    public static int simulationStepTime = 1000 / simulationStepsPerSecond;
    public static boolean[][] cells = new boolean[window.mainView.getHeight()][window.mainView.getWidth()];
    public static boolean[] mouseKeys = new boolean[]{false, false};
    //    public static boolean[] mouseLock = new boolean[]{false, false};
    public static boolean paused = true, pauseLock = false;

    public static void main(String[] args) {
        initMouse();
        initKeyboard();
        while (true) {
            long time = System.currentTimeMillis();
            if (time - prevFrame > FrameTime) {
                prevFrame = time;
                if (time - prevSimStep > simulationStepTime && !paused) {
                    prevSimStep = time;
                    simulate();
                }
                if (mouseKeys[0]) window.mainView.drawPixel();
                if (mouseKeys[1]) window.mainView.moveView();
                window.repaint();
            }
        }
    }

    public static void simulate() {
        // If the cell is alive, then it is alive if it has 2 or 3 live neighbors
        // If the cell is dead , then it is alive if it has      3 live neighbors

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
        temp.setColor(Color.BLACK);
        temp.fillRect(0, 0, w, h);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (cells[i][j]) result.setRGB(j, i, Color.WHITE.getRGB());
            }
        }
        return result;
    }

    public static void togglePause() {
        paused = !paused;
        pauseLock = true;
    }

    private static void initMouse() { // TODO: move to window class
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
//                    mouseLock[0] = false;
                    pauseLock = false;
                    window.mainView.prevPos.setLocation(-1, -1);
                }
                if (e.getButton() == 3) {
                    mouseKeys[1] = false;
//                    mouseLock[1] = false;
                    window.mainView.setPrevViewPos(-1d, -1d);
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
        window.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                window.mainView.changeScale(e.getWheelRotation());
            }
        });
    }

    private static void initKeyboard() { // TODO: move to window class?
        window.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int a = 0;
                if (e.getKeyChar() == '=') {
                    a = 1;
                } else if (e.getKeyChar() == '-') {
                    a = -1;
                }
                window.mainView.changeScale(a);
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