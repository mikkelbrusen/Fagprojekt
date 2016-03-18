package competition.fagprojekt;

import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class MarioMove {

    public static final float RunAcceleration = 1.2f;
    public static final float WalkAcceleration = 0.6f;
    public static final float Gravity = 3f;
    public static final float GroundInertia = 0.89f;
    public static final float FallInertia = 0.85f;
    public static final float JumpSpeed = -1.89f;

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

    public static boolean[] newAction() {
        return new boolean[Environment.numberOfKeys];
    }
    public static boolean[] moveAction(int dir, boolean doJump) {
        boolean[] a = newAction();
        a[Mario.KEY_SPEED] = true;
        a[Mario.KEY_RIGHT] = dir == 1;
        a[Mario.KEY_LEFT] = dir == -1;
        a[Mario.KEY_JUMP] = doJump;
        return a;
    }
}
