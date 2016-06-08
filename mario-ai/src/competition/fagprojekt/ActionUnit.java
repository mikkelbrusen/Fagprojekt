package competition.fagprojekt;

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
}
