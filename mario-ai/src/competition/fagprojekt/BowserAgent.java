package competition.fagprojekt;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.MarioVisualComponent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

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

        // Update target pos
        if(!worldSpace.rightMostWalkables.isEmpty())
            targetPos = worldSpace.rightMostWalkables.get(0);

        if(currentActions == null || currentActions.isEmpty()) {
            List<boolean[]> path = pathfinder.searchAStar(marioMove.lastCell, targetPos);
            if(path == null || path.isEmpty()) {
                currentActions.add(MarioMove.newAction()); // Do nothing
                System.out.println("No path: " + marioMove.lastCell + " -> " + targetPos);
            }
            else
                currentActions.addAll(path);
        }

        if(currentActions.size() > 1) {
            int nothing = 0;
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

