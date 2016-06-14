package competition.fagprojekt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mikkel on 07/06/16.
 */
public class JumpPath implements Serializable{
    ActionUnit actionUnit;
    List<Vec2i> collisionCells = new ArrayList<>();

    public JumpPath(){
        actionUnit = new ActionUnit();
    }

    public void addAction(boolean[] action){actionUnit.add(action);}

    public List<boolean[]> getActions(){
        return actionUnit.actions;
    }

    public void printBlocked() {
        for (Vec2i c : collisionCells)
            System.out.printf("B: %s\n", c);
    }
}
