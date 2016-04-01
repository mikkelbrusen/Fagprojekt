package competition.fagprojekt;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.environments.Environment;
import competition.fagprojekt.Debug.Debug;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BowserAgent extends BasicMarioAIAgent implements Agent
{
    WorldSpace worldSpace;
    Pathfinder pathfinder;
    MarioMove marioMove;

    Vec2i targetPos;

    List<boolean[]> currentActions = new ArrayList<>();

    public BowserAgent()
    {
        super("BowserAgent");
        reset();
    }

    public boolean[] getAction()
    {
        worldSpace.printWorldSpace();
        if(worldSpace.rightMostWalkables.isEmpty())
            currentActions.add(MarioMove.newAction());

        if (currentActions == null || currentActions.isEmpty()) {
            for(Vec2i targetCell : worldSpace.rightMostWalkables) {
                targetPos = targetCell;
                List<boolean[]> path = pathfinder.searchAStar(marioMove.lastCell, targetPos);
                if (path == null || path.isEmpty()) {
                    currentActions.add(MarioMove.newAction()); // Do nothing
                    System.out.println("No path: " + marioMove.lastCell + " -> " + targetPos);
                } else {
                    currentActions.addAll(path);
                    break;
                }
            }
        }

        action = currentActions.get(0);
        currentActions.remove(0);

        /*
        boolean isJumping = action[Mario.KEY_JUMP];
        boolean willJump = marioMove.lastCell.x >= 4;

        if(!isJumping && willJump) {
            framesJumping = 0;
            startY = marioMove.lastFloatPos.y;
            System.out.println("START DEBUG");
        }

        if(willJump) {
            framesJumping++;
        }

        if(framesJumping > 0) {
            float h = marioMove.lastFloatPos.y - startY;
            System.out.println(framesJumping + ": " + h);
        }
        action[Mario.KEY_JUMP] = willJump;
        */

        Debug debug = Debug.getInstance();
        Vec2f p0 = marioMove.lastFloatPos;
        Vec2f v0 = marioMove.velocity;

        int w = 20;
        int h = 20;
        for(int i = 0; i < w; i++) {
            for(int j = 0; j < h; j++) {
                int x = marioMove.lastCell.x - (w / 2) + j;
                int y = marioMove.lastCell.y - (h / 2) + i;
                Vec2i p1 = new Vec2i(x, y);

                Color color = marioMove.canJumpToCell(p0, v0, p1)
                        ? Color.blue : Color.black;

                debug.drawCell(p1, color);
            }
        }

        debug.drawCell(marioMove.lastCell);

        for(Vec2i rightMost : worldSpace.rightMostWalkables)
            debug.drawCell(rightMost, Color.gray);

        if(targetPos != null)
            debug.drawCell(targetPos, Color.green);

        return action;
    }

    @Override
    public void integrateObservation(Environment environment)
    {
        super.integrateObservation(environment);

        worldSpace.integrateObservation(environment);
        marioMove.integrateObservation(environment);
    }

    public void reset()
    {
        worldSpace = new WorldSpace();
        marioMove = new MarioMove();
        pathfinder = new Pathfinder(worldSpace, marioMove);
    }
}

