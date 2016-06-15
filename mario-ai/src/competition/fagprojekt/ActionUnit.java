package competition.fagprojekt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by max on 28/04/16.
 * A collection of actions.
 * One ActionUnit will contain the actions for moving from one cell to another.
 */
public class ActionUnit implements Serializable{
    private List<boolean[]> actions = new LinkedList<>(); // Constant add and ends
    public Vec2f endPosition = new Vec2f(0, 0);
    public Vec2f endVelocity = new Vec2f(0, 0);

    public ActionUnit() {
    }

    public void add(boolean[] action) {
        actions.add(action);
    }
    public void push(boolean[] action) {
        actions.add(0, action);
    }
    public void addAll(List<boolean[]> actions) {
        this.actions.addAll(actions);
    }
    public void pushAll(List<boolean[]> actions) {
        this.actions.addAll(0, actions);
    }

    public ActionUnit clone() {
        ActionUnit unit = new ActionUnit();
        if (endPosition != null)
            unit.endPosition = endPosition.clone();
        if (endVelocity != null)
            unit.endVelocity = endVelocity.clone();
        unit.addAll(getActions());
        return unit;
    }

    public List<boolean[]> getActions() {
        return Collections.unmodifiableList(actions);
    }
}
