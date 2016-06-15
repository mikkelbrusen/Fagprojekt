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
    final private int MAX_SEARCH_ITERATIONS = 2000;

    private WorldSpace worldSpace;

    public JumpPathfinder(WorldSpace worldSpace) {
        this.worldSpace = worldSpace;
    }

    public JumpPath searchAStar(Vec2f start, Vec2f startVelocity, Vec2f end) {
        return searchAStar(start, startVelocity, end, false);
    }

    public JumpPath searchAStar(Vec2f start, Vec2f startVelocity, Vec2f end, boolean takeBest) {
        Queue<JumpPathNode> open = new PriorityQueue<>();
        // No closed list, as every point is unique

        // Setup start node
        JumpPathNode current = new JumpPathNode(
                new SimMario(start, startVelocity, worldSpace),
                null, MarioMove.emptyAction(), 0, 0);

        // If takeBest == true, this will be returned
        JumpPathNode bestSeen = current;

        boolean hasFoundEnd = false;
        open.add(current);
        for(int i = 0; i < MAX_SEARCH_ITERATIONS; i++) {
            if (open.isEmpty())
                break;
            current = open.poll();

            if (bestSeen.getParent() == null || current.compareTo(bestSeen) < 0)
                bestSeen = current;

            if (isEnd(current, end)) {
                hasFoundEnd = true;
                break;
            }

            open.addAll(getNeighbours(current, start, end));
        }

        if (!hasFoundEnd) {
            if (!takeBest)
                return null;

            current = bestSeen;
            System.out.println("Didn't find end, taking best");
        }

        ActionUnit actionUnit = new ActionUnit(current.getSimMario().getPosition(),
                current.getSimMario().getVelocity());

        JumpPath path = new JumpPath(actionUnit);

        while (current.getParent() != null) {
            path.getActionUnit().push(current.getAction());

            // TODO: Refactor and fix
            Vec2f cp = current.getSimMario().getPosition().clone();
            cp.x -= start.x;
            cp.y -= start.y;
            cp.y += 2f; // Adjust for SimMario grounded
            path.addCollisionCells(SimMario.cellsBlocked(cp));

            current = current.getParent();
        }

        return path;
    }

    List<JumpPathNode> getNeighbours(JumpPathNode parent, Vec2f start, Vec2f end) {
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
            // The first action must jump
            if (parent.getParent() == null && !action[Environment.MARIO_KEY_JUMP])
                continue;

            // If the jump key has no effect, don't bother searching with it
            if (parent.hasStoppedJumping() && action[Environment.MARIO_KEY_JUMP])
                continue;

            // We only observe running actions
            if (!action[Environment.MARIO_KEY_SPEED])
                continue;

            // Perform the action
            SimMario newSimMario = parent.getSimMario().clone();
            newSimMario.move(action);

            Vec2f p = newSimMario.getPosition().clone();
            Vec2f v = newSimMario.getVelocity().clone();

            float score = 1 + parent.getFitness().getScoreTo();

            // Calculate the heuristic
            int framesX = Pathfinder.framesToRunTo(p.x, v.x, end.x);

            int jumpFrames = newSimMario.getJumpTime();
            int framesY = MarioMove.minimumFramesToMoveToY(p.y, v.y, jumpFrames, end.y);

            float heuristic = Math.max(framesX, framesY);

            // Encourage only moving in the direction of the target
            if(start.x < end.x && action[Environment.MARIO_KEY_LEFT])
                heuristic += 2f;
            else if(start.x > end.x && action[Environment.MARIO_KEY_RIGHT])
                heuristic += 2f;

            // Discourage searching impossible jumps
            if (p.y - 1.01f > end.y)
                heuristic += 100000;

            JumpPathNode n = new JumpPathNode(newSimMario, parent, action, score, heuristic);
            neighbours.add(n);
        }

        return neighbours;
    }

    boolean isEnd(JumpPathNode node, Vec2f end) {
        Vec2f p0 = node.getSimMario().getPosition().clone();
        Vec2f p1 = end.clone();
        Vec2f d = Vec2f.subtract(p1, p0);
        return Math.abs(d.x) < 3f && // Close in x
                Math.abs(d.y) < 4f && // To ensure not standing on another cell
                node.getSimMario().getMayJump();
    }

    public WorldSpace getWorldSpace() {
        return worldSpace;
    }
}
