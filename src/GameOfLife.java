import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class GameOfLife {
    public static ArrayList<Integer> alive = new ArrayList<>();
    public static Window window = new Window();
    public static long prevFrame = 0;
    public static long prevSimStep = 0;
    public static int simulationStepsPerSecond = 2;
    public static int simulationStepTime = 1000 / simulationStepsPerSecond;
    public static boolean[] mouseKeys = new boolean[]{false, false};

    public static void main(String[] args) {
        initMouse();
        while (true) {
            long time = System.currentTimeMillis();
            if (time - prevFrame > 10) {
                prevFrame = time;
                window.repaint();
            }
        }
    }

    private static void initMouse() {
        window.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Clicked");

            }

            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("Pressed");
                System.out.println(e.getX() + ":" + e.getY() + ":" + e.getXOnScreen() + ":" + e.getYOnScreen() + "  " + e.getButton());
                if (e.getButton() == 1) mouseKeys[0] = true;
                if (e.getButton() == 3) mouseKeys[1] = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("Released");
                if (e.getButton() == 1) mouseKeys[0] = false;
                if (e.getButton() == 3) mouseKeys[1] = false;

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                System.out.println("Entered");

            }

            @Override
            public void mouseExited(MouseEvent e) {
                System.out.println("Exited");

            }
        });
    }
}