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
        return searchAStar(start, startVelocity, end, false);
    }

    public JumpPath searchAStar(Vec2f start, Vec2f startVelocity, Vec2f end, boolean takeBest) {
        Queue<JumpPathNode> open = new PriorityQueue<>();
        // No closed list, as every point is unique

        // Setup start node
        JumpPathNode current = new JumpPathNode(
                new SimMario(start, startVelocity, worldSpace)
        );

        JumpPathNode bestSeen = null;

        boolean hasFoundEnd = false;
        open.add(current);
        for(int i = 0; i < MAX_SEARCH_ITERATIONS; i++) {
            if (open.isEmpty())
                break;
            current = open.poll();

            if (bestSeen == null || bestSeen.parent == null || current.compareTo(bestSeen) < 0)
                bestSeen = current;

            if (isEnd(current, end)) {
                hasFoundEnd = true;
                break;
            }

            open.addAll(getNeighbours(current,start, end));
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
            if (action[Environment.MARIO_KEY_JUMP])
                dist.y *= 0.5f;

            float heuristic = dist.magnitude();

            // Encourage falling if below
            if (!action[Environment.MARIO_KEY_JUMP] && p.y < end.y) // Above
                heuristic -= 2f;

            // Encourage only moving in the direction of the target
            if(start.x<end.x && action[Environment.MARIO_KEY_LEFT])
                heuristic +=3f;
            else if(start.x>end.x && action[Environment.MARIO_KEY_RIGHT])
                heuristic +=3f;

            // Discard impossible options
            final float nudge = 1f; // Room of error for floating calculations
            if (p.y - nudge > end.y && v.y > 0f) // Below and moving down
                heuristic += 10000f; // Impossible to recover from

            JumpPathNode n = new JumpPathNode(newSimMario, parent, action, score, heuristic);
            neighbours.add(n);
        }

        return neighbours;
    }

    // TODO: Now that we use the best found path if this never returns true,
    // maybe we should just always search the full iterations and take the
    // best possible found.
    boolean isEnd(JumpPathNode node, Vec2f end) {
        Vec2f p0 = node.simMario.body.position.clone();
        Vec2f p1 = end.clone();
        Vec2f d = Vec2f.subtract(p1, p0);
        return Math.abs(d.x) < 4f && // Close in x
                d.y < 4f && d.y >= -0.05; // Else slightly above
    }

    public WorldSpace getWorldSpace() {
        return worldSpace;
    }
}
