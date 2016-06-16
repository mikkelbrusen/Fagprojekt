package competition.fagprojekt;

import java.util.*;

public class Pathfinder {
    private WorldSpace worldSpace;
    private MarioMove marioMove; // CHECK ME
    private JumpTable jumpTable;

    final int MAX_MS = 35;

    List<Vec2i> lastPathCells = new ArrayList<>(); // Debug

    public Pathfinder(WorldSpace worldSpace, MarioMove marioMove, JumpTable jumpTable) {
        this.worldSpace = worldSpace;
        this.marioMove = marioMove;
        this.jumpTable = jumpTable;
    }

    // Returns a list of the actions needed, to move from start to end
    public List<ActionUnit> searchAStar(Vec2f start, Vec2f startVelocity, Vec2i end) {
        long startTimeInMs = System.currentTimeMillis();

        Queue<PathNode> open = new PriorityQueue<>();
        HashSet<Vec2i> closed = new HashSet<>(); // Constant time contains

        // Create current
        PathNode current = new PathNode(start.toCell(), null, 0, 0,
                new ActionUnit(start, startVelocity));

        closed.add(current.getCell().clone());
        open.add(current);

        boolean hasFoundEnd = false;
        while (!open.isEmpty()) {
            current = open.poll();
            closed.add(current.getCell().clone());

            // Bailout if going above time constraint
            long currentTimeInMs = System.currentTimeMillis();
            if (currentTimeInMs - startTimeInMs > MAX_MS) {
                BowserAgent.PathfindingBailedOut = true;
                return null;
            }

            if (current.getCell().equals(end)) {
                hasFoundEnd = true;
                break;
            }

            for (PathNode n : getNeighbours(current,end)) {
                if (!closed.contains(n.getCell())) {
                    open.add(n);
                }
            }
        }

        if (!hasFoundEnd)
            return null;

        lastPathCells.clear(); // Debug

        List<ActionUnit> path = new LinkedList<>();
        while (current.getParent() != null) {
            lastPathCells.add(current.getCell()); // Debug

            path.add(0, current.getActionUnit()); // Constant time
            current = current.getParent();
        }
        return path;
    }

    List<PathNode> getNeighbours(PathNode parent, Vec2i end) {
        List<PathNode> neighbours = new ArrayList<>();
        Vec2i pos = parent.getCell();

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
                Vec2i p0 = parent.getCell().clone(); // Origin cell
                Vec2i p1 = new Vec2i(p0.x + i - xOffset, p0.y + j - yOffset); // Target cell
                JumpPath jp = jumpTable.findPathRelative(i - xOffset, j - yOffset,
                        parent.getActionUnit().getEndVelocity().x, false);

                if (jp == null) // Only valid jumps
                    continue;

                if(!isWalkable(p1.x, p1.y))
                    continue;

                if (jp.hasCollision(p0, worldSpace))
                    continue;

                // JumpPaths endPosition is relative
                Vec2f endPosition = jp.getActionUnit().getEndPosition().clone();
                endPosition = Vec2f.add(endPosition, parent.getActionUnit().getEndPosition());

                int score = jp.getActionUnit().getActions().size();
                float heuristic = end.x - pos.x;

                ActionUnit actionUnit = new ActionUnit(endPosition, jp.getActionUnit().getEndVelocity());
                actionUnit.addAll(jp.getActions());

                PathNode node = new PathNode(p1, parent, score, heuristic, actionUnit);
                neighbours.add(node);
            }
        }

        return neighbours;
    }

    // Creates a pathnode for targetCell, with reference to parent
    public PathNode createWalkNode(Vec2i targetCell, PathNode parent, Vec2i end){
        // TODO: Use SimMario?
        // TODO: Optimize, very ineffecient
        Vec2f p0 = parent.getActionUnit().getEndPosition().clone();
        Vec2f v0 = parent.getActionUnit().getEndVelocity().clone();
        Vec2f p1 = targetCell.middleBottom();

        // Calculate run actions
        int dir = targetCell.x < parent.getCell().x ? -1 : 1;
        int runFrames = framesToRunTo(p0.x, v0.x, p1.x);

        // Calculate new velocity
        Vec2f newV = v0.clone();
        newV.x = xVelocityAfter(newV, runFrames, dir);

        // Calculate new position
        float newX = MarioMove.xPositionAfterRun(p0.x, v0.x, dir, runFrames);
        Vec2f endPos = new Vec2f(newX, p0.y);

       // Calculate the heuristic
        int framesX = Pathfinder.framesToRunTo(newX, newV.x, end.x);

        // Scoring
        int scoreForEdge = runFrames;
        float heuristic = framesX;

        ActionUnit actionUnit = new ActionUnit(endPos, newV);
        for (int i = 0; i < runFrames; i++)
            actionUnit.add(MarioMove.moveAction(dir, false));

        return new PathNode(targetCell, parent, scoreForEdge, heuristic, actionUnit);
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
