package competition.fagprojekt;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class BowserAgent extends BasicMarioAIAgent implements Agent
{
    WorldSpace worldSpace = new WorldSpace();

    public BowserAgent()
    {
        super("BowserAgent");
        reset();
    }

    public boolean[] getAction()
    {
        return action;
    }

    @Override
    public void integrateObservation(Environment environment)
    {
        super.integrateObservation(environment);

        worldSpace.integrateObservation(environment);
    }

    public void reset()
    {

    }
}

