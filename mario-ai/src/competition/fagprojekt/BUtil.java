package competition.fagprojekt;

import ch.idsia.benchmark.mario.environments.Environment;
import java.util.List;

/**
 * Created by max on 27/04/16.
 */
public class BUtil {
    public static String actionToString(boolean[] a) {
        if(a.length < 5)
            return "";

        return String.format("W=%s A=%s D=%s S=%s K=%s",
                a[Environment.MARIO_KEY_JUMP] ? "X" : "_",
                a[Environment.MARIO_KEY_LEFT] ? "X" : "_",
                a[Environment.MARIO_KEY_RIGHT] ? "X" : "_",
                a[Environment.MARIO_KEY_DOWN] ? "X" : "_",
                a[Environment.MARIO_KEY_SPEED] ? "X" : "_" );
    }

    public static void printPath(List<ActionUnit> path) {
        for (ActionUnit au : path) {
            for (boolean[] b: au.actions) {
                System.out.println(BUtil.actionToString(b));
            }
        }
    }

    public static void printActionUnit(ActionUnit unit) {
        for (boolean[] b: unit.actions) {
            System.out.println(BUtil.actionToString(b));
        }
    }
}
