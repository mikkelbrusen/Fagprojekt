package competition;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;

public class BowserAgent extends BasicMarioAIAgent implements Agent
{
    public BowserAgent()
    {
        super("BowserAgent");
        reset();
    }

    public boolean[] getAction()
    {
        return action;
    }

    public void reset()
    {

    }
}

