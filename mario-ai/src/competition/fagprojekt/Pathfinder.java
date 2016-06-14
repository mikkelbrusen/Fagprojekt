package competition.fagprojekt;

import java.util.*;
import java.util.List;

public class Pathfinder {
    WorldSpace worldSpace;
    MarioMove marioMove;
    JumpTable jumpTable;

    List<Vec2i> lastPathCells = new ArrayList<>(); // Debug

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

        lastPathCells.clear();
        List<ActionUnit> path = new ArrayList<>();
        while (current.parent != null) {
            lastPathCells.add(current.position);

            path.add(current.actions);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    List<PathNode> getNeighbours(PathNode parent, Vec2i end) {
        List<PathNode> neighbours = new ArrayList<>();
        Vec2i pos = parent.position;

        float heuristic = end.x - pos.x;

        for(Vec2i p : getWalkables(pos)) {
            if (!isWalkable(p.x, p.y))
                continue;
            neighbours.add(createNode(p, parent, end));
        }

        /*
        for (JumpPath jp : jumpTable.getAllJumpsFrom(parent.endBody.position, parent.endBody.velocity.x)) {
            Vec2i p = jp.actionUnit.endPosition.toCell();
            if (!isWalkable(p.x, p.y))
                continue;

            int score = jp.actionUnit.actions.size();

            PathNode node = new PathNode(p, parent, score, heuristic,
                jp.actionUnit.endPosition, jp.actionUnit.endVelocity);

            node.actions = jp.actionUnit;
            neighbours.add(node);
        }
        */

        int xOffset = jumpTable.xRange / 2;
        int yOffset = jumpTable.yRange / 2;
        for (int i = 0; i < jumpTable.jumpPathTable.length; i++) {
            for (int j = 0; j < jumpTable.jumpPathTable[0].length; j++) {
                Vec2i p0 = parent.position.clone();
                Vec2i p1 = new Vec2i(p0.x + i - xOffset, p0.y + j - yOffset);
                JumpPath jp = jumpTable.findPathRelative(i - xOffset, j - yOffset,
                        parent.actions.endVelocity.x, false);

                if (jp == null)
                    continue;

                if(!isWalkable(p1.x, p1.y))
                    continue;

                boolean anyCollision = false;
                for (Vec2i c : jp.collisionCells) {
                    Vec2i cp = Vec2i.add(c, p0);
                    Cell cell = worldSpace.getCell(cp.x, cp.y);
                    if (cell != null && !WorldSpace.isPassable(cell.type)) {
                        anyCollision = true;
                        break;
                    }
                }

                if (anyCollision)
                    continue;

                Vec2f endPosition = jp.actionUnit.endPosition.clone();
                endPosition = Vec2f.add(endPosition, parent.actions.endPosition);

                int score = jp.actionUnit.actions.size();
                PathNode node = new PathNode(p1, parent, score, heuristic,
                        endPosition, jp.actionUnit.endVelocity);

                node.actions = jp.actionUnit.clone();
                node.actions.endPosition = endPosition.clone();
                //node.actions.endVelocity = jp.actionUnit.endVelocity.clone();

                neighbours.add(node);
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
        Vec2f p0 = parent.actions.endPosition.clone();
        Vec2f v0 = parent.actions.endVelocity.clone();
        Vec2f p1 = p.toVec2f();
        p1.x += 0.5f * WorldSpace.CELL_WIDTH;

        // Calculate run actions
        int dir = p.x < parent.position.x ? -1 : 1;
        int runFrames = framesToRunTo(p0.x, v0.x, p1.x);

        Vec2f newV = v0.clone();
        newV.x = xVelocityAfter(newV, runFrames, dir);

        PathNode node = new PathNode(p);
        node.actions.endVelocity = newV.clone();
        node.actions.endPosition.x = MarioMove.xPositionAfterRun(p0.x, v0.x, dir, runFrames);
        node.actions.endPosition.y = p0.y;
        node.parent = parent;

        int scoreForEdge = runFrames; // TODO: Score run edge;
        float heuristic = (end.x - p1.x);

        for (int i = 0; i < runFrames; i++)
            node.actions.add(MarioMove.moveAction(dir, false));
        
        node.fitness.scoreTo = parent.fitness.scoreTo + scoreForEdge;
        node.fitness.heuristic = heuristic;

        return node;
    }

    // TODO: Refactor, to maybe use sign of difference
    public static int framesToRunTo(float x0, float v0, float x1) {
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
            vx += MarioMove.RunAcceleration * (float)dir;
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
