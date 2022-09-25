import java.util.ArrayList;

public class GameOfLife {
    public static int // ##### SETTINGS ##### //
            windowWidth = 960, windowHeight = 640, cellsWidth = 500, cellsHeight = 300, FPS = 100;
    public static boolean[][] cells = new boolean[cellsHeight][cellsWidth];
    public static boolean[] keys = new boolean[4], mouseKeys = new boolean[2];
    public static boolean paused = true, needToChangeSize;
    public static int frameSamples = 40, simulationStepsPerSecond = 10;
    public static long prevFrame = 0, prevSimStep = 0, frameTime = 1_000_000_000 / FPS, simulationStepTime = 1_000_000_000 / simulationStepsPerSecond;
    public static Window window = new Window(windowWidth, windowHeight);
    public static ArrayList<Long> timings = new ArrayList<>();

    public static void main(String[] args) {
        while (true) {
            long time = System.nanoTime();
            long frameDiff = 0;
            if (prevFrame > 1) frameDiff = time - prevFrame - frameTime;
            if (frameDiff >= 0) {
                if (prevFrame > 0) timings.add(time - prevFrame);
                if (timings.size() > frameSamples) timings.remove(0);
                prevFrame = time - (frameDiff - frameTime);
                simulationStepTime = 1_000_000_000 / simulationStepsPerSecond;
                long timeDiff = 0;
                if (prevSimStep > 1) timeDiff = time - prevSimStep - simulationStepTime;
                if (!paused && timeDiff >= 0) {
                    while (timeDiff >= 0 && !(timeDiff > simulationStepTime * 100)) {
                        prevSimStep += simulationStepTime;
                        timeDiff -= simulationStepTime;
                        simulate();
                    }
                    prevSimStep = time;
                }
                window.repaint();
                if (needToChangeSize) changeCellsSize();
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

    public static void changeSimStepTime(boolean direction) {
        if (direction) {
            if (simulationStepsPerSecond < 10) simulationStepsPerSecond++;
            simulationStepsPerSecond *= 1.2d;
        } else {
            simulationStepsPerSecond *= 0.9d;
        }
        if (simulationStepsPerSecond < 1) simulationStepsPerSecond = 1;
        if (simulationStepsPerSecond > 1000) simulationStepsPerSecond = 1000;
        Window.ControlPanel.Simspeed.updatePos();
    }

    public static void changeCellsSize() {
        cells = new boolean[cellsHeight][cellsWidth];
        needToChangeSize = false;
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
}