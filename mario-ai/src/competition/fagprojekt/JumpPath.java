package competition.fagprojekt;

import java.util.List;

/**
 * Created by Mikkel on 07/06/16.
 */
public class JumpPath {
    ActionUnit actionUnit;
    Vec2f velocity = new Vec2f(0, 0);

    public JumpPath(){
        actionUnit = new ActionUnit();
    }

    public void addAction(boolean[] action){actionUnit.add(action);}

    public List<boolean[]> getActions(){
        return actionUnit.actions;
    }

}
