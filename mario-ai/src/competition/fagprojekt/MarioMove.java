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
    public static final int MaxJumpFrames = 7;

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

    static final float MaxJumpHeight = 46.55f; // Calculated. See MarioMath.mw
    public static boolean canJumpToCell(Vec2f p0, Vec2f v0, Vec2i p1) {
        // If doing a maximum jump, is the cell reachable?
        Vec2f diff = Vec2f.subtract(WorldSpace.cellToFloat(p1), p0);

        Vec2f endP = WorldSpace.cellToFloat(p1);
        Vec2f p = p0.clone();
        Vec2f v = v0.clone();

        int facing = diff.x < 0 ? -1 :
                diff.x > 0 ? 1 : 0;

        int jumpTime = 7; // Look at Mario line 370 for this jumping code
        while(facing == 1 && p.x < endP.x || facing == -1 && p.x > endP.x) {
            // Apply jump, if possible
            if(jumpTime > 0) {
                v.y = jumpTime * -1.9f;
                jumpTime--;
            }

            // Accelerate sideways
            v.x += RunAcceleration * facing;

            // Move
            p = Vec2f.add(p, v);

            // Apply inertia
            v.x *= GroundInertia;
            v.y *= FallInertia;

            // Apply gravity
            v.y += Gravity;
        }

        return p.y < endP.y; // Check if we're above the target cell now
    }

    public static int minimumJumpFramesToEndAtHeight(float y0, float y1) {
        for(int i = 0; i <= MaxJumpFrames; i++) { // Try every jump frame combination
            if(bodyAfterJumpAndFall(y0, i, i).position <= y1)
                return i;
        }
        return -1; // TODO: What to return here? Implicit check for possibility?
    }
    public static int minimumJumpFramesToEndAtHeightAfterFrames(float y0, float y1, int runFrames) {
        for(int i = 0; i <= MaxJumpFrames; i++) { // Try every jump frame combination
            if(bodyAfterJumpAndFall(y0, i, runFrames).position <= y1)
                return i;
        }
        return -1; // TODO: What to return here? Implicit check for possibility?
    }

    public static int minimumFramesToMoveToY(float y0, float v0, int jumpFramesLeft, float y1) {
        int lowest = 10000;
        for (int i = 0; i <= jumpFramesLeft; i++) {
            int frames = minimumFramesToMoveToYWithJumpFrames(y0, v0, i, y1);
            lowest = Math.min(lowest, frames);
        }
        return lowest;
    }
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

            if (framesUsed > 100)
                break;
        }

        return framesUsed;
    }

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

    public static Body1D bodyAfterJumpAndFall(float y, int jumpFrames, int totalFrames) {
        Body1D yBody = new Body1D(y, 0); // Jumping reset vertical velocity, so we can ignore it as input
        for(int i = 0; i < totalFrames; i++) {
            if(i < jumpFrames)
                yBody.velocity = (MaxJumpFrames - i) * JumpSpeed;

            yBody.position += yBody.velocity;

            yBody.velocity *= FallInertia;
            yBody.velocity += Gravity;
        }

        return yBody;
    }

    public static Body2D bodyAfterAction(Body2D body, boolean[] action) {
        Body2D endBody = new Body2D(body.position, body.velocity);

        return endBody;
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
