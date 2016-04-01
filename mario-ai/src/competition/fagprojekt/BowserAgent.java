package competition.fagprojekt;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.environments.Environment;
import competition.fagprojekt.Debug.Debug;

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
        // worldSpace.printWorldSpace();
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
        /*
        Vec2f p0 = marioMove.lastFloatPos;
        Vec2f p1 = Vec2f.add(p0, new Vec2f(0, 32));
        debug.drawLine(p0, p1);
        */
        debug.drawCell(marioMove.lastCell);

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

