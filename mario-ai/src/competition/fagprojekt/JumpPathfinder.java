package competition.fagprojekt;

import ch.idsia.benchmark.mario.environments.Environment;

import java.util.*;

/**
 * Created by max on 06/06/16.
 */
public class JumpPathfinder
{
    // As there is no closed list, we must limit the search.
    // If the end isn't found in this number of iterations, it's deemed impossible
    final private int MAX_SEARCH_ITERATIONS = 1000;

    private WorldSpace worldSpace;

    public JumpPathfinder(WorldSpace worldSpace) {
        this.worldSpace = worldSpace;
    }

    public JumpPath searchAStar(Vec2f start, Vec2f startVelocity, Vec2f end) {
        // First search to above the target cell
        Vec2f upTarget = end.clone();
        upTarget.y -= 16f;

        JumpPath upPath = searchAStar(start, startVelocity, end, false, true);
        if (upPath == null) {
            return null; // If we can't find the first part, the jumps is impossible
        }

        JumpPath downPath = searchAStar(upPath.getActionUnit().getEndPosition(),
                upPath.getActionUnit().getEndVelocity(), end, false, false);

        if (downPath == null) {
            return null; // Need both parts of the jump to be successful
        }

        // Stitch the two paths together
        ActionUnit actionUnit = new ActionUnit(downPath.getActionUnit().getEndPosition(), downPath.getActionUnit().getEndVelocity());
        JumpPath endPath = new JumpPath(actionUnit);
        endPath.getActionUnit().addAll(upPath.getActions());
        endPath.getActionUnit().addAll(downPath.getActions());
        endPath.addCollisionCells(upPath.getCollisionCells());
        //endPath.collisionCells.addAll(downPath.collisionCells);

        return endPath;
    }

    public JumpPath searchAStar(Vec2f start, Vec2f startVelocity, Vec2f end, boolean takeBest, boolean isUp) {
        Queue<JumpPathNode> open = new PriorityQueue<>();
        // No closed list, as every point is unique

        // Setup start node
        JumpPathNode current = new JumpPathNode(
                new SimMario(start, startVelocity, worldSpace),
                null, MarioMove.emptyAction(), 0, 0);

        if (!isUp) {
            // Ensure the down-path doesn't try jumping
            current.stoppedJumping = true;
            current.simMario.jumpTime = 0;
        }

        // If takeBest == true, this will be returned
        JumpPathNode bestSeen = current;

        boolean hasFoundEnd = false;
        open.add(current);
        for(int i = 0; i < MAX_SEARCH_ITERATIONS; i++) {
            if (open.isEmpty())
                break;
            current = open.poll();

            if (bestSeen.parent == null || current.compareTo(bestSeen) < 0)
                bestSeen = current;

            if (isEnd(current, end, isUp)) {
                hasFoundEnd = true;
                break;
            }

            open.addAll(getNeighbours(current, start, end, isUp));
        }

        if (!hasFoundEnd) {
            if (!takeBest)
                return null;

            current = bestSeen;
            System.out.println("Didn't find end, taking best");
        }

        ActionUnit actionUnit = new ActionUnit(current.simMario.body.position,
                current.simMario.body.velocity);

        JumpPath path = new JumpPath(actionUnit);

        while (current.parent != null) {
            path.getActionUnit().push(current.action);

            // TODO: Refactor and fix
            Vec2f cp = current.simMario.body.position.clone();
            cp.x -= start.x;
            cp.y -= start.y;
            cp.x += 0.5f * WorldSpace.CELL_WIDTH;
            cp.y += WorldSpace.CELL_HEIGHT;
            path.addCollisionCells(SimMario.cellsBlocked(cp));

            current = current.parent;
        }

        return path;
    }

    List<JumpPathNode> getNeighbours(JumpPathNode parent, Vec2f start, Vec2f end, boolean isUp) {
        // Left, Right, Down, Jump, Speed
        final boolean[][] possibleActions = {
                { false, false, false, false, false },
                { false, false, false, false, true },
                { false, false, false, true, false },
                { false, false, false, true, true },
                { false, true, false, false, false },
                { false, true, false, false, true },
                { false, true, false, true, false },
                { false, true, false, true, true },
                { true, false, false, false, false },
                { true, false, false, false, true },
                { true, false, false, true, false },
                { true, false, false, true, true },
// Left + Right is unnecessary
//               { true, true, false, false, false },
//               { true, true, false, false, true },
//               { true, true, false, true, false },
//               { true, true, false, true, true },
        };

        List<JumpPathNode> neighbours = new ArrayList<>();
        for (boolean[] action : possibleActions) {
            // If the jump key has no effect, don't bother searching with it
            if (parent.stoppedJumping && action[Environment.MARIO_KEY_JUMP])
                continue;

            // We only observe running actions
            if (!action[Environment.MARIO_KEY_SPEED])
                continue;

            // Perform the action
            SimMario newSimMario = parent.simMario.clone();
            newSimMario.move(action);

            Vec2f p = newSimMario.body.position.clone();
            Vec2f v = newSimMario.body.velocity.clone();

            float score = 1 + parent.fitness.scoreTo;

            // Calculate the heuristic
            Vec2f dist = Vec2f.subtract(end, p);
            float heuristic = dist.sqrMagnitude(); // When searching down, the base is the sqrDistance

            // When searching  upwards, where interested in the Manhattan frame distance
            if (isUp) {
                int framesX = Pathfinder.framesToRunTo(p.x, v.x, end.x);

                int jumpFrames = newSimMario.jumpTime;
                int framesY = MarioMove.minimumFramesToMoveToY(p.y, v.y, jumpFrames, end.y);

                heuristic = framesX + framesY;
            }

            // Encourage only moving in the direction of the target
            if(start.x < end.x && action[Environment.MARIO_KEY_LEFT])
                heuristic += 3f;
            else if(start.x > end.x && action[Environment.MARIO_KEY_RIGHT])
                heuristic += 3f;

            // Aim for a lower velocity
            heuristic += newSimMario.body.velocity.x;

            // Discourage searching impossible jumps
            if (!isUp && p.y - 1.01f > end.y)
                heuristic += 100000;

            JumpPathNode n = new JumpPathNode(newSimMario, parent, action, score, heuristic);
            neighbours.add(n);
        }

        return neighbours;
    }

    // The end criteria is different when searching up than when searching down
    boolean isEnd(JumpPathNode node, Vec2f end, boolean isSearchingUp) {
        return isSearchingUp ? isEndUp(node, end) : isEndDown(node, end);
    }

    boolean isEndUp(JumpPathNode node, Vec2f end) {
        Vec2f p0 = node.simMario.body.position.clone();
        Vec2f v0 = node.simMario.body.velocity.clone();
        Vec2f p1 = end.clone();
        Vec2f d = Vec2f.subtract(p1, p0);
        return Math.abs(d.x) < 16f && // Relatively close in x
                d.y > -8f && // Close in y
                Math.abs(v0.y) < 8f; // Top of jump
    }

    boolean isEndDown(JumpPathNode node, Vec2f end) {
        Vec2f p0 = node.simMario.body.position.clone();
        Vec2f p1 = end.clone();
        Vec2f d = Vec2f.subtract(p1, p0);
        return Math.abs(d.x) < 1f && // Close in x
                Math.abs(d.y) < 4f && // To ensure not standing on another cell
                node.simMario.onGround;
    }

    public WorldSpace getWorldSpace() {
        return worldSpace;
    }
}
