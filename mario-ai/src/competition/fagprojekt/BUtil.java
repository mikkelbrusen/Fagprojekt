package competition.fagprojekt;

import ch.idsia.benchmark.mario.environments.Environment;

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
}
