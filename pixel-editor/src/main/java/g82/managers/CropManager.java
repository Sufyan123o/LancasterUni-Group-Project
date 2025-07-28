package g82.managers;

public class CropManager {
    // Used by layer manager and canvas panel to keep track of selected pixels
    private static int lastX = -1;
    private static int lastY = -1;
    private static int firstX = -1;
    private static int firstY = -1;

    public static void Update(int x, int y) {
        if (firstX == -1) {
            firstX = x;
            lastX = x;
            firstY = y;
            lastY = y;
        } else {
            lastX = x;
            lastY = y;
        }
    }

    public static void Reset() {
        lastX = -1;
        lastY = -1;
        firstX = -1;
        firstY = -1;
    }

    public static boolean IsReset() {
        return lastX == -1 && lastY == -1 && firstX == -1 && firstY == -1;
    }

    public static int getLastX() {
        return lastX;
    }

    public static int getLastY() {
        return lastY;
    }
    public static int getFirstX() {
        return firstX;
    }
    public static int getFirstY() {
        return firstY;
    }
}
