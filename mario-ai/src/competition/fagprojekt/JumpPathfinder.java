package competition.fagprojekt;

import ch.idsia.benchmark.mario.environments.Environment;

import java.util.*;

/**
 * Created by max on 06/06/16.
 */
public class JumpPathfinder
{
    final int MAX_SEARCH_ITERATIONS = 1000;

    WorldSpace worldSpace;
    MarioMove marioMove;

    public JumpPathfinder(WorldSpace worldSpace, MarioMove marioMove) {
        this.worldSpace = worldSpace;
        this.marioMove = marioMove;
    }

    public JumpPath searchAStar(Vec2i start, Vec2f startVelocity, Vec2i end) {
        return searchAStar(WorldSpace.cellToFloat(start), startVelocity, WorldSpace.cellToFloat(end));
    }

    public JumpPath searchAStar(Vec2f start, Vec2f startVelocity, Vec2f end) {
        JumpPath upPath = searchAStar(start, startVelocity, end, false, true);
        if (upPath == null)
            return null;

        JumpPath downPath = searchAStar(upPath.actionUnit.endPosition, upPath.actionUnit.endVelocity, end, false, false);
        if (downPath == null) {
            searchAStar(upPath.actionUnit.endPosition, upPath.actionUnit.endVelocity, end, false, false);
            return null;
        }

        JumpPath endPath = new JumpPath();
        endPath.actionUnit.endVelocity = downPath.actionUnit.endVelocity.clone();
        endPath.actionUnit.endPosition = downPath.actionUnit.endPosition.clone();
        endPath.actionUnit.actions.addAll(upPath.getActions());
        endPath.actionUnit.actions.addAll(downPath.getActions());

        return endPath;
    }

    public JumpPath searchAStar(Vec2f start, Vec2f startVelocity, Vec2f end, boolean takeBest, boolean isUp) {
        Queue<JumpPathNode> open = new PriorityQueue<>();
        // No closed list, as every point is unique

        // Setup start node
        JumpPathNode current = new JumpPathNode(
                new SimMario(start, startVelocity, worldSpace)
        );
        if (!isUp) {
            current.stoppedJumping = true;
            current.simMario.jumpTime = 0;
        }

        JumpPathNode bestSeen = null;

        boolean hasFoundEnd = false;
        open.add(current);
        for(int i = 0; i < MAX_SEARCH_ITERATIONS; i++) {
            if (open.isEmpty())
                break;
            current = open.poll();

            if (bestSeen == null || bestSeen.parent == null || current.compareTo(bestSeen) < 0)
                bestSeen = current;

            if (isUp && isEndUp(current, end) ||
                    !isUp && isEnd(current, end))
            {
                hasFoundEnd = true;
                break;
            }

            open.addAll(getNeighbours(current,start, end, isUp));
        }

        if (!hasFoundEnd) {
            if (!takeBest)
                return null;

            current = bestSeen;
            System.out.println("Didn't find end, taking best");
        }

        JumpPath path = new JumpPath();
        path.actionUnit.endPosition = current.simMario.body.position.clone();
        path.actionUnit.endVelocity = current.simMario.body.velocity.clone();

        while (current.parent != null) {
            path.addAction(current.action);
            current = current.parent;
        }
        Collections.reverse(path.actionUnit.actions);
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
            if (parent.stoppedJumping && action[Environment.MARIO_KEY_JUMP])
                continue;
            if (!action[Environment.MARIO_KEY_SPEED])
                continue;

            SimMario newSimMario = parent.simMario.clone();
            newSimMario.move(action);

            Vec2f p = newSimMario.body.position.clone();
            Vec2f v = newSimMario.body.velocity.clone();

            // Heuristic is in cell distance, score is in frames? Problem?
            float score = 1 + parent.fitness.scoreTo;

            // TODO: Maybe wrap into single function
            // Don't stop jumping the second we reach the correct height
            // (dist.y is lowest here)
            Vec2f dist = Vec2f.subtract(end, p);

            float heuristic = dist.sqrMagnitude();

            if (isUp)
                heuristic = Vec2f.subtract(end, Vec2f.add(p, v)).sqrMagnitude();

            if (!isUp && p.y - 1.01f > end.y)
                heuristic += 100000;

            JumpPathNode n = new JumpPathNode(newSimMario, parent, action, score, heuristic);
            neighbours.add(n);
        }

        return neighbours;
    }

    // TODO: Now that we use the best found path if this never returns true,
    // maybe we should just always search the full iterations and take the
    // best possible found.
    boolean isEndUp(JumpPathNode node, Vec2f end) {
        Vec2f p0 = node.simMario.body.position.clone();
        Vec2f v0 = node.simMario.body.velocity.clone();
        Vec2f p1 = end.clone();
        Vec2f d = Vec2f.subtract(p1, p0);
        return Math.abs(d.x) < 0.8f * WorldSpace.CellWidth &&
                d.y > 10f && d.y < 32f &&
                Math.abs(v0.y) < 4f; // End of jump
    }

    boolean isEnd(JumpPathNode node, Vec2f end) {
        Vec2f p0 = node.simMario.body.position.clone();
        Vec2f p1 = end.clone();
        Vec2f d = Vec2f.subtract(p1, p0);
        return Math.abs(d.x) < 4f && // Close in x
               //Math.abs(d.y) < 4f &&
               node.simMario.onGround;
    }

    public WorldSpace getWorldSpace() {
        return worldSpace;
    }
}
