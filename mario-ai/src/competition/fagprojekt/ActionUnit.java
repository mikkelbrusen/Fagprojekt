package competition.fagprojekt;

import ch.idsia.benchmark.mario.environments.Environment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by max on 28/04/16.
 * A collection of actions.
 * One ActionUnit will contain the actions for moving from one cell to another.
 */
public class ActionUnit implements Serializable{
    public List<boolean[]> actions = new ArrayList<>();
    public Vec2f endPosition = new Vec2f(0, 0);
    public Vec2f endVelocity = new Vec2f(0, 0);

    public ActionUnit() {
    }

    public void add(boolean[] action) {
        actions.add(action);
    }

    public ActionUnit clone() {
        ActionUnit unit = new ActionUnit();
        if (endPosition != null)
            unit.endPosition = endPosition.clone();
        if (endVelocity != null)
            unit.endVelocity = endVelocity.clone();
        unit.actions.addAll(actions);
        return unit;
    }
}
