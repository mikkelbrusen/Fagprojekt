package competition.fagprojekt;

import competition.fagprojekt.Debug.Debug;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Pathfinder {
    WorldSpace worldSpace;
    MarioMove marioMove;

    public Pathfinder(WorldSpace worldSpace, MarioMove marioMove) {
        this.worldSpace = worldSpace;
        this.marioMove = marioMove;
    }

    // We have to return a list of moves, where a move
    // is defined as the move from one position to the next
    // eg. a jump or a run from one cell to the next
    public List<boolean[]> searchAStar(Vec2i start, Vec2i end) {
        Queue<PathNode> open = new PriorityQueue<>();
        List<Vec2i> closed = new LinkedList<>(); // TODO: Should be hash table for best complexity, but we need to override hashCode() then
        PathNode current = new PathNode(start.clone());

        boolean hasFoundEnd = false;
        closed.add(current.position.clone());
        open.add(current);
        while (!open.isEmpty()) {
            current = open.poll();
            closed.add(current.position.clone());

            if (current.position.equals(end)) {
                hasFoundEnd = true;
                break;
            }

            for (PathNode n : getNeighbours(current)) {
                if (!closed.contains(n.position)) {
                    open.add(n);
                }
            }
        }

        if (!hasFoundEnd)
            return null;

        List<boolean[]> path = new ArrayList<>();
        while (current.parent != null) {
            Debug.getInstance().drawCell(current.position, Color.white);

            path.addAll(current.actions);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    List<PathNode> getNeighbours(PathNode parent) {
        List<PathNode> neighbours = new ArrayList<>();

        Vec2i pos = parent.position;

        for(Vec2i p : getWalkables(pos)) {
            if (!isWalkable(p.x, p.y))
                continue;

            neighbours.add(createNode(p,parent,false));
        }

        for(Vec2i p : getJumpables(pos)) {
            if (!isWalkable(p.x, p.y))
                continue;

            neighbours.add(createNode(p,parent,true));
        }

        if (isEmpty(pos.x, pos.y + 1)) { // Falling
            Vec2f newV = parent.marioVelocity.clone();
            newV.y = yVelocityAfterFalling(parent.marioVelocity, 1);

            int scoreForFalling = 10;
            neighbours.add(new PathNode(new Vec2i(pos.x, pos.y + 1), parent, scoreForFalling, newV));
        }

        return neighbours;
    }

    public PathNode createNode(Vec2i p, PathNode parent, boolean isJump){
        // Calculate run actions
        int dir = p.x < parent.position.x ? -1 : 1;
        int runFrames = framesToRunTo(parent.position, parent.marioVelocity, p);

        Vec2f newV = parent.marioVelocity.clone();
        newV.x = xVelocityAfter(parent.marioVelocity, runFrames, dir);

        PathNode node = new PathNode(p);
        node.marioVelocity.x = newV.x;
        node.parent = parent;

        int scoreForEdge = 0;
        if(!isJump){
            scoreForEdge = runFrames; // TODO: Score run edge

            //PathNode node = new PathNode(p, parent, scoreForEdge, newV);
            for(int i = 0; i < runFrames; i++)
                node.actions.add(MarioMove.moveAction(dir, false));

        } else {  // is jump
            // Calculate jump actions first
            float h = (p.y - parent.position.y) * WorldSpace.CellHeight; // Height of jump
            int jumpFrames = 7; // TODO: Actually calculate the number of jump frames. Remember frames for falling too

            newV.y = yVelocityAfterJumping(parent.marioVelocity, jumpFrames);
            node.marioVelocity.y =  newV.y;

            int framesNeeded = Math.max(jumpFrames, runFrames);
            scoreForEdge = framesNeeded; // TODO: Weight properly

            boolean doJump;
            for(int i = 0; i < framesNeeded; i++) {
                doJump = i < jumpFrames;
                node.actions.add(marioMove.moveAction(dir, doJump));
            }
        }

        node.scoreTo = scoreForEdge;
        return node;
    }

    // TODO: Refactor, to maybe use sign of difference
    static int framesToRunTo(Vec2i p0, Vec2f v0, Vec2i p1) {
        float v = v0.x;
        float x = p0.x * WorldSpace.CellWidth;
        float x1 = p1.x * WorldSpace.CellWidth;
        int t = 0;
        if(x < x1) { // Moving right
            while(x < x1) {
                v += MarioMove.RunAcceleration;
                x += v;
                v *= MarioMove.GroundInertia;
                t++;
            }
        }
        else if(x1 < x) {
            while(x1 < x) {
                v -= MarioMove.RunAcceleration;
                x += v;
                v *= MarioMove.GroundInertia;
                t++;
            }
        }

        // t == 0, if on the same cell
        return t;
    }

    // TODO: Maybe put these functions in MarioMove?
    // TODO: Makes this return x-velocity of Mario, after moving in direction
    // dir (left = -1, right = 1) for frames frames
    static float xVelocityAfter(Vec2f v0, int frames, int dir) {
        float vx = v0.x;
        for(int i = 0; i < frames; i++)
            vx += MarioMove.RunAcceleration * Math.pow(MarioMove.GroundInertia, i + 1);
        return vx;
    }
    static float yVelocityAfterFalling(Vec2f v0, int frames) {
        float vy = v0.y;
        for(int i = 0; i < frames; i++)
            vy += MarioMove.Gravity * Math.pow(MarioMove.FallInertia, i + 1);
        return vy;
    }
    static float yVelocityAfterJumping(Vec2f v0, int frames) {
        return frames <= 1
                ? MarioMove.JumpSpeed * 7
                : MarioMove.JumpSpeed * (8 - frames);
    }

    static Vec2i[] getWalkables(Vec2i p) {
        return new Vec2i[] {
                new Vec2i(p.x - 1, p.y),
                new Vec2i(p.x + 1, p.y)
        };
    }
    static Vec2i[] getJumpables(Vec2i p) {
        return new Vec2i[] {
                new Vec2i(p.x - 1, p.y - 1),
                new Vec2i(p.x + 1, p.y - 1),
                new Vec2i(p.x - 1, p.y + 1),
                new Vec2i(p.x + 1, p.y + 1),
        };
    }
    boolean isEmpty(int x, int y) {
        return isType(x, y, CellType.Empty);
    }
    boolean isWalkable(int x, int y) {
        return isType(x, y, CellType.Walkable);
    }
    boolean isSolid(int x, int y) {
        return isType(x, y, CellType.Solid);
    }

    boolean isType(int x, int y, CellType type) {
        return isType(worldSpace.getCell(x, y), type);
    }
    boolean isType(Cell cell, CellType type) {
        return cell != null && cell.type == type;
    }
}
