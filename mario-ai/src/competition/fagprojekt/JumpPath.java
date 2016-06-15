package competition.fagprojekt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Mikkel on 07/06/16.
 */
public class JumpPath implements Serializable{
    private ActionUnit actionUnit;
    private List<Vec2i> collisionCells = new LinkedList<>();

    public JumpPath(ActionUnit actionUnit) {
        this.actionUnit = actionUnit.clone();
    }

    public void addCollisionCell(Vec2i cell) {
        if (!collisionCells.contains(cell))
            collisionCells.add(cell.clone());
    }
    public void addCollisionCells(List<Vec2i> cells) {
        for (Vec2i c : cells) {
            addCollisionCell(c);
        }
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

    public List<boolean[]> getActions(){
        return actionUnit.getActions();
    }

    public List<Vec2i> getCollisionCells() {
        return Collections.unmodifiableList(collisionCells);
    }

    public ActionUnit getActionUnit() {
        return actionUnit;
    }
}
