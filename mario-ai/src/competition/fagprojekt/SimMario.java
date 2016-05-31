package competition.fagprojekt;

/**
 * Created by max on 31/05/16.
 *
 * Simulates a Mario for moving.
 * TODO: Use MarioMove values everywhere possible
 * TODO: Initialize with Mario actual values
 * TODO: Implement collision
 */
public class SimMario
{
    static final int KEY_LEFT = 0;
    static final int KEY_RIGHT = 1;
    static final int KEY_DOWN = 2;
    static final int KEY_JUMP = 3;
    static final int KEY_SPEED = 4;
    static final int KEY_UP = 5;

    static final float GROUND_AND_INERTIA = 0.89f;

    int facing = 1;

    boolean sliding = false;
    boolean ducking = false;
    boolean onGround = true;
    boolean mayJump = true;

    int jumpTime;
    int runTime;

    float xJumpSpeed;
    float yJumpSpeed;

    public Body2D body = new Body2D(new Vec2f(0, 0), new Vec2f(0, 0));

    public SimMario(Vec2f position, Vec2f velocity)
    {
        body.position = position.clone();
        body.velocity = velocity.clone();
    }

    public void move(boolean[] keys)
    {
        float x = body.position.x;
        float y = body.position.y;
        float xa = body.velocity.x;
        float ya = body.velocity.x;

        // Ladder logic was here

        // Win and death here

        // wasOnGround = onGround;

        float sideWaysSpeed = keys[KEY_SPEED] ? 1.2f : 0.6f;

        if (onGround)
        {
            ducking = keys[KEY_DOWN]; // && large;
        }

        if (xa > 2)
        {
            facing = 1;
        }
        if (xa < -2)
        {
            facing = -1;
        }

        if (keys[KEY_JUMP] || (jumpTime < 0 && !onGround && !sliding))
        {
            if (jumpTime < 0)
            {
                xa = xJumpSpeed;
                ya = -jumpTime * yJumpSpeed;
                jumpTime++;
            } else if (onGround && mayJump)
            {
                xJumpSpeed = 0;
                yJumpSpeed = -1.9f;
                jumpTime = 7;
                ya = jumpTime * yJumpSpeed;
                onGround = false;
                sliding = false;
            } else if (sliding && mayJump)
            {
                xJumpSpeed = -facing * 6.0f;
                yJumpSpeed = -2.0f;
                jumpTime = -6;
                xa = xJumpSpeed;
                ya = -jumpTime * yJumpSpeed;
                onGround = false;
                sliding = false;
                facing = -facing;
            } else if (jumpTime > 0)
            {
                xa += xJumpSpeed;
                ya = jumpTime * yJumpSpeed;
                jumpTime--;
            }
        } else
        {
            jumpTime = 0;
        }

        if (keys[KEY_LEFT] && !ducking)
        {
            if (facing == 1) sliding = false;
            xa -= sideWaysSpeed;
            if (jumpTime >= 0) facing = -1;
        }

        if (keys[KEY_RIGHT] && !ducking)
        {
            if (facing == -1) sliding = false;
            xa += sideWaysSpeed;
            if (jumpTime >= 0) facing = 1;
        }

        if ((!keys[KEY_LEFT] && !keys[KEY_RIGHT]) || ducking || ya < 0 || onGround)
        {
            sliding = false;
        }

        // Shot here

        // Cheats here

        // ableToShoot = !keys[KEY_SPEED];

        mayJump = (onGround || sliding) && !keys[KEY_JUMP];

        runTime += (Math.abs(xa)) + 5;
        if (Math.abs(xa) < 0.5f)
        {
            runTime = 0;
            xa = 0;
        }

        // calcPic();

        if (sliding)
        {
           ya *= 0.5f;
        }

        onGround = false;

        // Actually move
        x += xa;
        y += ya;

        // OOB check here

        if (x < 0)
        {
            x = 0;
            xa = 0;
        }

        // More OOB check here
        ya *= MarioMove.FallInertia;
        xa *= MarioMove.GroundInertia;

        if (!onGround)
        {
            ya += MarioMove.Gravity;
        }

        // Carried

        body.position = new Vec2f(x, y);
        body.velocity = new Vec2f(xa, ya);
    }
}
