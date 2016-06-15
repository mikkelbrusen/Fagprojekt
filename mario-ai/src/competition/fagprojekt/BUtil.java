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
        for (ActionUnit unit : path) {
            Vec2i dp = unit.getEndPosition() == null ? new Vec2i(0, 0) : unit.getEndPosition().toCell();
            System.out.printf("Unit to %s:\n", unit.getEndPosition() == null ? "(x, x)" : dp);
            for (boolean[] a : unit.getActions())
                System.out.printf("  %s\n", BUtil.actionToString(a));
        }
    }

    public static void printActionUnit(ActionUnit unit) {
        for (boolean[] b: unit.getActions()) {
            System.out.println(BUtil.actionToString(b));
        }
    }

    public static void printWorldSpace(WorldSpace worldSpace)
    {
        Vec2i tableSize = worldSpace.getSize();

         for(int i = 0; i < 16; i++) { // Row = Y
            String line = String.format("%2d:", i);
            for(int j = 0; j < tableSize.x; j++) { // Col = X
                Cell c = worldSpace.getCell(j, i);
                String v = c == null ? "." :
                    c.type == CellType.Empty || c.type == CellType.Coin ? "0" :
                    c.type == CellType.Walkable ? "X" : "1";

                line += v + " ";
            }
            System.out.println(line);
        }

        System.out.println();
    }
}
