import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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
            if (time - prevFrame > 25) {
                prevFrame = time;
                window.repaint();
                if (mouseKeys[0]) window.mainView.drawPixel();
                if (mouseKeys[1]) window.mainView.moveView();
            }
        }
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
                if (e.getButton() == 1) mouseKeys[0] = false;
                if (e.getButton() == 3) mouseKeys[1] = false;
                if (e.getButton() == 1) window.mainView.prevPos.setLocation(-1, -1);
                if (e.getButton() == 3) window.mainView.prevViewPos.setLocation(-1, -1);

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