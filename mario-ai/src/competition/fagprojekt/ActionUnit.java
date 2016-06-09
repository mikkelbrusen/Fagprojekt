package competition.fagprojekt;

import ch.idsia.benchmark.mario.environments.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by max on 28/04/16.
 * A collection of actions.
 * One ActionUnit will contain the actions, for moving from one cell to another.
 */
public class ActionUnit {
    public List<boolean[]> actions = new ArrayList<>();
    public Vec2f endPosition;

    public ActionUnit() {
    }

    public void add(boolean[] action) {
        actions.add(action);
    }

    public ActionUnit clone() {
        ActionUnit unit = new ActionUnit();
        if (endPosition != null)
            unit.endPosition = endPosition.clone();
        unit.actions.addAll(actions);
        return unit;
    }

    public void reverseActionDirections(){
        for (boolean[] a: actions) {
            boolean temp = a[Environment.MARIO_KEY_LEFT];
            a[Environment.MARIO_KEY_LEFT] = a[Environment.MARIO_KEY_RIGHT];
            a[Environment.MARIO_KEY_RIGHT] = temp;
        }
    }
}
