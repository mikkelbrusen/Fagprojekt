package competition.fagprojekt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by max on 31/05/16.
 *
 * Simulates a Mario for moving.
 * TODO: Use MarioMove values everywhere possible
 */
public class SimMario
{
    static final int KEY_LEFT = 0;
    static final int KEY_RIGHT = 1;
    static final int KEY_DOWN = 2;
    static final int KEY_JUMP = 3;
    static final int KEY_SPEED = 4;
    static final int KEY_UP = 5;

    int facing = 1;

    boolean sliding = false;
    boolean ducking = false;
    boolean onGround = true;
    boolean mayJump = true;

    int jumpTime;
    int runTime;

    float xJumpSpeed;
    float yJumpSpeed;

    float x, y;
    float xa, ya;

    public Body2D body = new Body2D(new Vec2f(0, 0), new Vec2f(0, 0));

    WorldSpace worldSpace;

    public SimMario(Vec2f position, Vec2f velocity, WorldSpace worldSpace)
    {
        body.position = position.clone();
        body.velocity = velocity.clone();

        x = position.x;
        y = position.y;
        xa = velocity.x;
        ya = velocity.y;

        this.worldSpace = worldSpace;
    }

    public SimMario clone() {
        SimMario newSimMario = new SimMario(body.position, body.velocity, worldSpace);

        newSimMario.facing = facing;

        newSimMario.sliding = sliding;
        newSimMario.ducking = ducking;
        newSimMario.onGround = onGround;
        newSimMario.mayJump = mayJump;

        newSimMario.jumpTime = jumpTime;
        newSimMario.runTime = runTime;

        newSimMario.xJumpSpeed = xJumpSpeed;
        newSimMario.yJumpSpeed = yJumpSpeed;

        return newSimMario;
    }

    public void move(boolean[] keys)
    {
        /*
        float x = body.position.x;
        float y = body.position.y;
        float xa = body.velocity.x;
        float ya = body.velocity.y;
        */

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
        move(xa, 0);
        move(0, ya);
        //x += xa;
        //y += ya;

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

    private boolean move(float xa, float ya)
    {
        while (xa > 8)
        {
            if (!move(8, 0)) return false;
            xa -= 8;
        }
        while (xa < -8)
        {
            if (!move(-8, 0)) return false;
            xa += 8;
        }
        while (ya > 8)
        {
            if (!move(0, 8)) return false;
            ya -= 8;
        }
        while (ya < -8)
        {
            if (!move(0, -8)) return false;
            ya += 8;
        }

        // Pulled from Mario
        final float width = 4; // Half of Mario's width
        final float height = 24;

        boolean collide = false;
        if (ya > 0)
        {
            if (isBlocking(x + xa - width, y + ya, xa, 0)) collide = true;
            else if (isBlocking(x + xa + width, y + ya, xa, 0)) collide = true;
            else if (isBlocking(x + xa - width, y + ya + 1, xa, ya)) collide = true;
            else if (isBlocking(x + xa + width, y + ya + 1, xa, ya)) collide = true;
        }
        if (ya < 0)
        {
            if (isBlocking(x + xa, y + ya - height, xa, ya)) collide = true;
            else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
            else if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
        }
        if (xa > 0)
        {
            sliding = true;
            if (isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
            else sliding = false;
            if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya)) collide = true;
            else sliding = false;
            if (isBlocking(x + xa + width, y + ya, xa, ya)) collide = true;
            else sliding = false;
        }
        if (xa < 0)
        {
            sliding = true;
            if (isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
            else sliding = false;
            if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya)) collide = true;
            else sliding = false;
            if (isBlocking(x + xa - width, y + ya, xa, ya)) collide = true;
            else sliding = false;
        }

        if (collide)
        {
            if (xa < 0)
            {
                x = (int) ((x - width) / 16) * 16 + width;
                this.xa = 0;
            }
            if (xa > 0)
            {
                x = (int) ((x + width) / 16 + 1) * 16 - width - 1;
                this.xa = 0;
            }
            if (ya < 0)
            {
                y = (int) ((y - height) / 16) * 16 + height;
                jumpTime = 0;
                this.ya = 0;
            }
            if (ya > 0)
            {
                y = (int) ((y - 1) / 16 + 1) * 16 - 1;
                onGround = true;
            }
            return false;
        } else
        {
            x += xa;
            y += ya;
            return true;
        }
    }

    // We keep xa and ya parameters for possible future use - some blocks allows passing from one direction,
    // e.g. ledges you can stand on but jump through
    private boolean isBlocking(final float _x, final float _y, final float xa, final float ya)
    {
        Vec2i cellPos = WorldSpace.floatToCell(new Vec2f(_x, _y));
        int x = cellPos.x;
        int y = cellPos.y;

        /*
        int x = (int) (_x / 16);
        int y = (int) (_y / 16);
        */

        if (x == (int)(this.x / 16) && y == (int)(this.y / 16)) return false;

        Cell cell = worldSpace.getCell(x, y);
        //System.out.println(String.format("SimMario = (%f, %f)", this.x, this.y));
        //System.out.println(String.format("(%d, %d): %s", x, y,
                //cell == null ? "Null" : cell.type.toString()));

        return cell != null && !WorldSpace.isPassable(cell.type);
    }

    public static List<Vec2i> cellsBlocked(Vec2f p) {
        /*
        Point layout:
        0   1


        3 ! 2
        */

        final float w = 4f;
        final float h = 24f;

        Vec2f p0 = Vec2f.add(p, new Vec2f(-w + 1f, -h + 1f));
        Vec2f p1 = Vec2f.add(p, new Vec2f(w - 1f, -h + 1f));
        Vec2f p2 = Vec2f.add(p, new Vec2f(w - 1f, -1f));
        Vec2f p3 = Vec2f.add(p, new Vec2f(-w + 1f, -1f));

        List<Vec2i> points = new ArrayList<>();
        points.add(p0.toCell());
        if (!points.contains(p1.toCell()))
            points.add(p1.toCell());
        if (!points.contains(p2.toCell()))
            points.add(p2.toCell());
        if (!points.contains(p3.toCell()))
            points.add(p3.toCell());

        return points;
    }
}
