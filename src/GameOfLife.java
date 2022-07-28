import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class GameOfLife {
    public static ArrayList<Integer> alive = new ArrayList<>();
    public static Window window = new Window();
    public static long prevFrame = 0;
    public static long prevSimStep = 0;
    public static int simulationStepsPerSecond = 2;
    public static int simulationStepTime = 1000 / simulationStepsPerSecond;
    public static boolean[][] cells = new boolean[window.mainView.getHeight()][window.mainView.getHeight()];
    public static boolean[] mouseKeys = new boolean[]{false, false};
    public static boolean paused = true, pauseLock = false;

    public static void main(String[] args) {
        initMouse();
        while (true) {
            long time = System.currentTimeMillis();
            if (time - prevFrame > 10) {
                prevFrame = time;
                if (!paused) simulate();

                window.repaint();
                if (mouseKeys[0]) window.mainView.drawPixel();
                if (mouseKeys[1]) window.mainView.moveView();
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

    public static BufferedImage drawCells(Graphics2D g2d) {
        BufferedImage result = new BufferedImage(window.mainView.getWidth(), window.mainView.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int h = cells.length, w = cells[0].length;
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
                    pauseLock = false;
                    window.mainView.prevPos.setLocation(-1, -1);
                }
                if (e.getButton() == 3) {
                    mouseKeys[1] = false;
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
}