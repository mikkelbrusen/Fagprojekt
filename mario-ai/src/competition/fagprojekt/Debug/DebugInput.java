package competition.fagprojekt.Debug;

import ch.idsia.benchmark.mario.engine.sprites.Mario;

import java.awt.event.KeyEvent;

/**
 * Created by max on 07/04/16.
 */
public class DebugInput {
    public static final int KEY_LEFT = 0;
    public static final int KEY_RIGHT = 1;
    public static final int KEY_UP = 2;
    public static final int KEY_DOWN = 3;

    public static boolean[] keysPressed = new boolean[4];

    public static void keyPressed(KeyEvent e) {
        toggleKey(e.getKeyCode(), true);
    }

    public static void keyReleased(KeyEvent e) {
        toggleKey(e.getKeyCode(), false);
    }

    private static void toggleKey(int keyCode, boolean isPressed) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                keysPressed[KEY_LEFT] = isPressed;
                break;
            case KeyEvent.VK_RIGHT:
                keysPressed[KEY_RIGHT] = isPressed;
                break;
            case KeyEvent.VK_DOWN:
                keysPressed[KEY_DOWN] = isPressed;
                break;
            case KeyEvent.VK_UP:
                keysPressed[KEY_UP] = isPressed;
                break;
        }
    }
}
