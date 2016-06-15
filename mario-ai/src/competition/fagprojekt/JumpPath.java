package competition.fagprojekt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mikkel on 07/06/16.
 */
public class JumpPath implements Serializable{
    ActionUnit actionUnit;
    List<Vec2i> collisionCells = new ArrayList<>(); // TODO: Make private

    public JumpPath(){
        actionUnit = new ActionUnit();
    }

    public void addAction(boolean[] action){actionUnit.add(action);}

    public List<boolean[]> getActions(){
        return actionUnit.actions;
    }

    // Returns true if any collision cells, relative to origin, aren't passable
    public boolean hasCollision(Vec2i origin, WorldSpace worldSpace) {
         for (Vec2i c : collisionCells) {
            Vec2i p1 = Vec2i.add(c, origin);
            Cell cell = worldSpace.getCell(p1.x, p1.y);
            if (cell != null && !WorldSpace.isPassable(cell.type)) {
                return true;
            }
        }
        return false;
    }

    public void printBlocked() {
        for (Vec2i c : collisionCells)
            System.out.printf("B: %s\n", c);
    }
}
