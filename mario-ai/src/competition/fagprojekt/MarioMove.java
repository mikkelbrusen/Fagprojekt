package competition.fagprojekt;

import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class MarioMove {
    // Values pulled from Mario.java
    public static final float RunAcceleration = 1.2f;
    public static final float WalkAcceleration = 0.6f;
    public static final float Gravity = 3f;
    public static final float GroundInertia = 0.89f;
    public static final float FallInertia = 0.85f;
    public static final float JumpSpeed = -1.89f;
    public static final int MaxJumpFrames = 7;

    // Keeps track of last observed Mario position and velocity
    public Vec2i lastCell;
    public Vec2f lastFloatPos;

    public Vec2f velocity;

    public void integrateObservation(Environment env) {
        Vec2i marioPos = WorldSpace.getMarioCellPos(env);
        Vec2f floatPos = WorldSpace.getMarioFloatPos(env);

        velocity = lastFloatPos != null ? Vec2f.subtract(floatPos, lastFloatPos) : new Vec2f(0, 0);

        lastFloatPos = floatPos.clone();
        lastCell = marioPos.clone();
    }

    // Calculates the minimum number of frames to move from y0 to y1
    // with start velocity v0 and maximum number of jumpFrames
    public static int minimumFramesToMoveToY(float y0, float v0, int jumpFramesLeft, float y1) {
        int lowest = 10000;
        for (int i = 0; i <= jumpFramesLeft; i++) {
            int frames = minimumFramesToMoveToYWithJumpFrames(y0, v0, i, y1);
            lowest = Math.min(lowest, frames);
        }
        return lowest;
    }

    // Calculates the minimum number of frames to move from y0 to y1
    // with start velocity v0 and jumping in jumpFrames frames
    public static int minimumFramesToMoveToYWithJumpFrames(float y0, float v0, int jumpFrames, float y1) {
        float y = y0;
        float v = v0;
        int d = (int)Math.signum(y1 - y0);

        int framesUsed = 0;
        while ((d == 1 && y < y1) || (d == -1 && y > y1))
        {
            // If jumps left
            if (jumpFrames > 0) {
                v = jumpFrames * -1.9f;
                jumpFrames--;
            }

            y += v;
            v *= MarioMove.FallInertia;
            v += MarioMove.Gravity;

            framesUsed++;

            // More than 100 frames, it definitely impossible
            if (framesUsed > 100)
                break;
        }

        return framesUsed;
    }

    // Calculates the x-position after running in frames frames
    // with start x x0, start velocity v0.
    // dir == -1 is left, dir == 1 is right
    public static float xPositionAfterRun(float x0, float v0, int dir, int frames) {
        float x = x0;
        float v = v0;
        for (int i = 0; i < frames; i++) {
            // Accelerate sideways
            v += RunAcceleration * (float)dir;

            // Move
            x += v;

            // Apply inertia
            v *= GroundInertia;
        }

        return x;
    }

    // Returns an empty action
    public static boolean[] newAction() {
        return new boolean[Environment.numberOfKeys];
    }

    // Returns a an action where Mario moves in direction dir
    // and a jump if doJump == true
    public static boolean[] moveAction(int dir, boolean doJump) {
        boolean[] a = newAction();
        a[Mario.KEY_SPEED] = true;
        a[Mario.KEY_RIGHT] = dir == 1;
        a[Mario.KEY_LEFT] = dir == -1;
        a[Mario.KEY_JUMP] = doJump;
        return a;
    }
}
