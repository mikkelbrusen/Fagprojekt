package competition.fagprojekt;

import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class MarioMove {

    public Vector2i lastCell = new Vector2i(0, 0);
    public Vector2i velocity = new Vector2i(0, 0);

    public void integrateObservation(Environment env) {
        Vector2i marioPos = WorldSpace.getMarioWorldPos(env);

        velocity = Vector2i.subtract(marioPos, lastCell);
        lastCell = WorldSpace.getMarioWorldPos(env);
    }

    public boolean[] actionsTowardsCell(Vector2i cell) {
        Vector2i diff = Vector2i.subtract(cell, lastCell);

        boolean[] actions = new boolean[Environment.numberOfKeys];
        if(diff.x < 0)
            actions[Mario.KEY_LEFT] = true;
        else if(diff.x > 0)
            actions[Mario.KEY_RIGHT] = true;

        return actions;
    }
}
