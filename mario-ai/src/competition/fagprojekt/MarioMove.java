package competition.fagprojekt;

import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class MarioMove {

    public Vec2i lastCell = new Vec2i(0, 0);
    public Vec2f lastFloatPos = new Vec2f(0, 0);

    public Vec2f velocity = new Vec2f(0, 0);

    public void integrateObservation(Environment env) {
        Vec2i marioPos = WorldSpace.getMarioCellPos(env);
        Vec2f floatPos = WorldSpace.getMarioFloatPos(env);

        velocity = Vec2f.subtract(floatPos, lastFloatPos);
        lastFloatPos = floatPos.clone();
        lastCell = marioPos.clone();
    }

    public boolean[] actionsTowardsCell(Vec2i cell) {
        Vec2i diff = Vec2i.subtract(cell, lastCell);

        boolean[] actions = new boolean[Environment.numberOfKeys];
        if(diff.x < 0) {
            actions[Mario.KEY_LEFT] = true;
        }
        else if(diff.x > 0) {
            actions[Mario.KEY_RIGHT] = true;
        }

        return actions;
    }
}
