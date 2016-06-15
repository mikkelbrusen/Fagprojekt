package competition.fagprojekt;

import java.util.*;

public class Pathfinder {
    private WorldSpace worldSpace;
    private MarioMove marioMove; // CHECK ME
    private JumpTable jumpTable;

    List<Vec2i> lastPathCells = new ArrayList<>(); // Debug

    public Pathfinder(WorldSpace worldSpace, MarioMove marioMove, JumpTable jumpTable) {
        this.worldSpace = worldSpace;
        this.marioMove = marioMove;
        this.jumpTable = jumpTable;
    }

    // Returns a list of the actions needed, to move from start to end
    public List<ActionUnit> searchAStar(Vec2f start, Vec2f startVelocity, Vec2i end) {
        Queue<PathNode> open = new PriorityQueue<>();
        HashSet<Vec2i> closed = new HashSet<>(); // Constant time contains

        // Create current
        PathNode current = new PathNode(start.toCell());
        current.actions = new ActionUnit(start, startVelocity);

        closed.add(current.position.clone());
        open.add(current);

        boolean hasFoundEnd = false;
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

        lastPathCells.clear(); // Debug

        List<ActionUnit> path = new LinkedList<>();
        while (current.parent != null) {
            lastPathCells.add(current.position); // Debug

            path.add(0, current.actions); // Constant time
            current = current.parent;
        }
        return path;
    }

    List<PathNode> getNeighbours(PathNode parent, Vec2i end) {
        List<PathNode> neighbours = new ArrayList<>();
        Vec2i pos = parent.position;

        // Add neighbours we can walk to
        for(Vec2i p : getWalkables(pos)) {
            if (!isWalkable(p.x, p.y))
                continue;
            neighbours.add(createWalkNode(p, parent, end));
        }

        // Check all possible jumps
        int xOffset = jumpTable.xRange / 2;
        int yOffset = jumpTable.yRange / 2;
        for (int i = 0; i < jumpTable.jumpPathTable.length; i++) {
            for (int j = 0; j < jumpTable.jumpPathTable[0].length; j++) {
                Vec2i p0 = parent.position.clone(); // Origin cell
                Vec2i p1 = new Vec2i(p0.x + i - xOffset, p0.y + j - yOffset); // Target cell
                JumpPath jp = jumpTable.findPathRelative(i - xOffset, j - yOffset,
                        parent.actions.getEndVelocity().x, false);

                if (jp == null) // Only valid jumps
                    continue;

                if(!isWalkable(p1.x, p1.y))
                    continue;

                if (jp.hasCollision(p0, worldSpace))
                    continue;

                // JumpPaths endPosition is relative
                Vec2f endPosition = jp.getActionUnit().getEndPosition().clone();
                endPosition = Vec2f.add(endPosition, parent.actions.getEndPosition());

                int score = jp.getActionUnit().getActions().size();
                float heuristic = end.x - pos.x;

                PathNode node = new PathNode(p1, parent, score, heuristic,
                        endPosition, jp.getActionUnit().getEndVelocity());

                node.actions = new ActionUnit(endPosition, jp.getActionUnit().getEndVelocity());
                node.actions.addAll(jp.getActionUnit().getActions());

                neighbours.add(node);
            }
        }

        return neighbours;
    }

    // Creates a pathnode for targetCell, with reference to parent
    public PathNode createWalkNode(Vec2i targetCell, PathNode parent, Vec2i end){
        // TODO: Use SimMario?
        // TODO: Optimize, very ineffecient
        Vec2f p0 = parent.actions.getEndPosition().clone();
        Vec2f v0 = parent.actions.getEndVelocity().clone();
        Vec2f p1 = targetCell.middleBottom();

        // Calculate run actions
        int dir = targetCell.x < parent.position.x ? -1 : 1;
        int runFrames = framesToRunTo(p0.x, v0.x, p1.x);

        Vec2f newV = v0.clone();
        newV.x = xVelocityAfter(newV, runFrames, dir);

        // TODO: Delete superfluous constructor
        PathNode node = new PathNode(targetCell);
        float newX = MarioMove.xPositionAfterRun(p0.x, v0.x, dir, runFrames);
        Vec2f endPos = new Vec2f(newX, p0.y);
        node.actions = new ActionUnit(endPos, newV);
        node.parent = parent;

        int scoreForEdge = runFrames;
        float heuristic = (end.x - p1.x);

        for (int i = 0; i < runFrames; i++)
            node.actions.add(MarioMove.moveAction(dir, false));
        
        node.fitness.scoreTo = parent.fitness.scoreTo + scoreForEdge;
        node.fitness.heuristic = heuristic;

        return node;
    }

    // Returns the number of frames required for running from x0,
    // with start velocity v0 past x1
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
        else if(x1 < x) { // Moving left
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

    static Vec2i[] getWalkables(Vec2i p) {
        return new Vec2i[] {
                new Vec2i(p.x - 1, p.y),
                new Vec2i(p.x + 1, p.y)
        };
    }

    boolean isWalkable(int x, int y) {
        return isType(x, y, CellType.Walkable);
    }

    boolean isType(int x, int y, CellType type) {
        return isType(worldSpace.getCell(x, y), type);
    }
    boolean isType(Cell cell, CellType type) {
        return cell != null && cell.type == type;
    }
}
