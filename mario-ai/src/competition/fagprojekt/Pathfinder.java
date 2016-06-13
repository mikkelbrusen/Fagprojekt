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
    public List<ActionUnit> searchAStar(Vec2f start, Vec2f startVelocity, Vec2i end) {
        Queue<PathNode> open = new PriorityQueue<>();
        List<Vec2i> closed = new LinkedList<>(); // TODO: Should be hash table for best complexity, but we need to override hashCode() then
        PathNode current = new PathNode(start.toCell());
        current.endBody.velocity = startVelocity.clone();
        current.endBody.position = start.clone();
        current.actions.endVelocity = startVelocity.clone();
        current.actions.endPosition = start.clone();

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

        float heuristic = end.x - pos.x;

        for(Vec2i p : getWalkables(pos)) {
            if (!isWalkable(p.x, p.y))
                continue;
            //System.out.println("Vel: "+parent.marioVelocity.x);
            neighbours.add(createNode(p, parent, end));
        }

        /*for(Vec2i p : getJumpables(floatPos, parent.marioVelocity)) {
            if (!isWalkable(p.x, p.y))
                continue;

            neighbours.add(createNode(p,parent,true, end));
        }*/

        int xOffset = jumpTable.xRange/2;
        int yOffset = jumpTable.yRange/2;
        int velIndex = jumpTable.getVelocityIdx(parent.endBody.velocity.x);
        for (int i = 0; i < jumpTable.jumpPathTable.length; i++) {
            for (int j = 0; j < jumpTable.jumpPathTable[0].length; j++) {
                JumpPath jp = jumpTable.jumpPathTable[i][j][velIndex];
                if(jp!=null){
                    Vec2i p = new Vec2i(parent.position.x+i-xOffset,parent.position.y+j-yOffset);

                    if(isWalkable(p.x, p.y)){
                        Vec2f endPosition = jp.actionUnit.endPosition.clone();
                        endPosition = Vec2f.add(endPosition, parent.endBody.position);

                        int score = jp.actionUnit.actions.size();
                        PathNode node = new PathNode(p, parent, score, heuristic,
                                endPosition, jp.actionUnit.endVelocity);

                        node.actions = jp.actionUnit;
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

    public PathNode createNode(Vec2i p, PathNode parent, Vec2i end){
        // TODO: Use SimMario?
        // TODO: Optimize, very ineffecient
        Vec2f p0 = parent.endBody.position.clone();
        Vec2f v0 = parent.endBody.velocity.clone();

        // Calculate run actions
        int dir = p.x < parent.position.x ? -1 : 1;
        int runFrames = framesToRunTo(p0.x, v0.x, p.toVec2f().x);

        Vec2f newV = parent.actions.endVelocity;
        newV.x = xVelocityAfter(newV, runFrames, dir);

        PathNode node = new PathNode(p);
        node.endBody.velocity.x = newV.x;
        node.endBody.position.x = MarioMove.xPositionAfterRun(p0.x, v0.x, runFrames);
        node.parent = parent;

        int scoreForEdge = runFrames; // TODO: Score run edge;
        int heuristic = (end.x - p.x);

        for (int i = 0; i < runFrames; i++)
            node.actions.add(MarioMove.moveAction(dir, false));
        
        node.actions.endPosition = node.endBody.position.clone();
        node.actions.endVelocity = node.endBody.velocity.clone();

        node.fitness.scoreTo = parent.fitness.scoreTo + scoreForEdge;
        node.fitness.heuristic = heuristic;

        return node;
    }

    // TODO: Refactor, to maybe use sign of difference
    static int framesToRunTo(float x0, float v0, float x1) {
        float v = v0;
        float x = x0;
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
