package competition.fagprojekt;

import competition.fagprojekt.Debug.Debug;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Pathfinder {
    WorldSpace worldSpace;
    MarioMove marioMove;
    JumpTable jumpTable;

    public Pathfinder(WorldSpace worldSpace, MarioMove marioMove, JumpTable jumpTable) {
        this.worldSpace = worldSpace;
        this.marioMove = marioMove;
        this.jumpTable = jumpTable;
    }

    // We have to return a list of moves, where a move
    // is defined as the move from one position to the next
    // eg. a jump or a run from one cell to the next
    public List<ActionUnit> searchAStar(Vec2i start, Vec2f startVelocity, Vec2i end) {
        Queue<PathNode> open = new PriorityQueue<>();
        List<Vec2i> closed = new LinkedList<>(); // TODO: Should be hash table for best complexity, but we need to override hashCode() then
        PathNode current = new PathNode(start.clone());
        current.marioVelocity = startVelocity.clone();

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

            for (PathNode n : getNeighbours(current,end)) {
                if (!closed.contains(n.position)) {
                    open.add(n);
                }
            }
        }

        if (!hasFoundEnd)
            return null;

        List<ActionUnit> path = new ArrayList<>();
        while (current.parent != null) {
            Debug.getInstance().drawCell(current.position, Color.green);

            path.add(current.actions);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    List<PathNode> getNeighbours(PathNode parent, Vec2i end) {
        List<PathNode> neighbours = new ArrayList<>();

        Vec2i pos = parent.position;
        Vec2f floatPos = WorldSpace.cellToFloat(pos);

        int heuristic = end.x - pos.x;

        for(Vec2i p : getWalkables(pos)) {
            if (!isWalkable(p.x, p.y))
                continue;
            //System.out.println("Vel: "+parent.marioVelocity.x);
            neighbours.add(createNode(p,parent,false, end));
        }

        /*for(Vec2i p : getJumpables(floatPos, parent.marioVelocity)) {
            if (!isWalkable(p.x, p.y))
                continue;

            neighbours.add(createNode(p,parent,true, end));
        }*/

        int xOffset = jumpTable.xRange/2;
        int yOffset = jumpTable.yRange/2;
        int velIndex = jumpTable.getVelocityIdx(parent.marioVelocity.x);
        for (int i = 0; i < jumpTable.jumpPathTable.length; i++) {
            for (int j = 0; j < jumpTable.jumpPathTable[0].length; j++) {
                JumpPath jp = jumpTable.jumpPathTable[i][j][velIndex];
                if(jp!=null){
                    Vec2i p = new Vec2i(parent.position.x+i-xOffset,parent.position.y+j-yOffset);

                    if(isWalkable(p.x,p.y)){
                        PathNode node = new PathNode(p, parent, jp.actionUnit.actions.size(), heuristic, jp.velocity);
                        node.actions = jp.actionUnit;
                        node.actions.endPosition = p.toVec2f();
                        node.actions.endVelocity = jp.velocity.clone();
                        neighbours.add(node);
                    }
                }
            }
        }
        /*
        if (isEmpty(pos.x, pos.y + 1)) { // Falling
            Vec2f newV = parent.marioVelocity.clone();
            newV.y = yVelocityAfterFalling(parent.marioVelocity, 1);

            int scoreForFalling = 10;
            neighbours.add(new PathNode(new Vec2i(pos.x, pos.y + 1), parent, scoreForFalling, heuristic, newV));
        }
        */
        return neighbours;
    }

    public PathNode createNode(Vec2i p, PathNode parent, boolean isJump, Vec2i end){
        // Calculate run actions
        int dir = p.x < parent.position.x ? -1 : 1;
        int runFrames = framesToRunTo(parent.position, parent.marioVelocity, p);

        Vec2f newV = parent.marioVelocity.clone();
        newV.x = xVelocityAfter(parent.marioVelocity, runFrames, dir);

        PathNode node = new PathNode(p);
        node.marioVelocity.x = newV.x;
        node.parent = parent;

        int scoreForEdge;
        int heuristic = (end.x - p.x);

        if(!isJump){
            scoreForEdge = runFrames; // TODO: Score run edge

            //PathNode node = new PathNode(p, parent, scoreForEdge, newV);
            for(int i = 0; i < runFrames; i++)
                node.actions.add(MarioMove.moveAction(dir, false));
            node.actions.endPosition = end.toVec2f();
            node.actions.endVelocity = newV.clone();

        } else {  // Is jump
            float y0f = parent.position.y * WorldSpace.CellHeight;
            float y1f = p.y * WorldSpace.CellHeight;

            int jumpFrames = MarioMove.minimumJumpFramesToEndAtHeight(y0f, y1f);
            int fallFrames = jumpFrames; // TODO: Actually calculate these too

            int framesNeeded = Math.max(jumpFrames + fallFrames, runFrames);

            Body1D yBody = MarioMove.bodyAfterJumpAndFall(y0f, jumpFrames, framesNeeded);
            newV.y = yBody.velocity;
            node.marioVelocity.y =  newV.y;

            scoreForEdge = framesNeeded; // TODO: Weight properly

            boolean doJump;
            for(int i = 0; i < framesNeeded; i++) {
                doJump = i < jumpFrames;
                int moveDir = dir * (i < runFrames ? 1 : 0); // Might not run for all frames
                node.actions.add(MarioMove.moveAction(moveDir, doJump));
            }
            Collections.reverse(node.actions.actions);
        }

        node.fitness.scoreTo = parent.fitness.scoreTo + scoreForEdge;
        node.fitness.heuristic = heuristic;
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
        for(int i = 0; i < frames; i++) {
            vx += MarioMove.RunAcceleration;
            vx *= MarioMove.GroundInertia;
        }
        return vx;
    }
    static float yVelocityAfterFalling(Vec2f v0, int frames) {
        float vy = v0.y;
        for(int i = 0; i < frames; i++) {
            vy *= MarioMove.FallInertia;
            vy += MarioMove.Gravity;
        }
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

    static List<Vec2i> getJumpables(Vec2f p0, Vec2f v0) {
        List<Vec2i> targets = new ArrayList<>();

        Vec2i cp0 = WorldSpace.floatToCell(p0);
        int w = 20;
        int h = 20;
        for(int i = 0; i < w; i++) {
            for(int j = 0; j < h; j++) {
                int x = cp0.x - (w / 2) + j;
                int y = cp0.y - (h / 2) + i;
                Vec2i p1 = new Vec2i(x, y);

                if(MarioMove.canJumpToCell(p0, v0, p1))
                    targets.add(p1);
            }
        }
        
        

        return targets;
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
