package competition.fagprojekt;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.environments.Environment;

import java.util.List;

public class BowserAgent extends BasicMarioAIAgent implements Agent
{
    WorldSpace worldSpace;
    Pathfinder pathfinder;
    MarioMove marioMove;

    Vector2i targetPos;

    public BowserAgent()
    {
        super("BowserAgent");
        reset();
}

    public boolean[] getAction()
    {
        targetPos = new Vector2i(11, 13);
        System.out.println("Mario: " + marioMove.lastCell + " -> " + targetPos);
        List<Vector2i> path = pathfinder.searchBfs(marioMove.lastCell, targetPos);
        if(path != null && !path.isEmpty()) {
            Vector2i nextCell = path.get(0);

            System.out.println("  -> " + nextCell);
            action = marioMove.actionsTowardsCell(nextCell);
        }

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
        pathfinder = new Pathfinder(worldSpace);
        marioMove = new MarioMove();
    }
}

