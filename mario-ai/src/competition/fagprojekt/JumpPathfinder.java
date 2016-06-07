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

    public ActionUnit searchAStar(Vec2i start, Vec2f startVelocity, Vec2i end) {
        Queue<JumpPathNode> open = new PriorityQueue<>();
        // No closed list, as every point is unique

        Vec2f endFloat = WorldSpace.cellToFloat(end);

        // Setup start node
        JumpPathNode current = new JumpPathNode(
                new SimMario(WorldSpace.cellToFloat(start),
                startVelocity.clone(),
                worldSpace)
        );

        boolean hasFoundEnd = false;
        open.add(current);
        for(int i = 0; i < MAX_SEARCH_ITERATIONS; i++) {
            if (open.isEmpty())
                break;
            current = open.poll();

            Vec2f endDist = Vec2f.subtract(endFloat, current.simMario.body.position);
            if (Math.abs(endDist.x) < 6f && endDist.y < 1.001f && endDist.y > -6f) {
                hasFoundEnd = true;
                break;
            }

            open.addAll(getNeighbours(current, end));
        }

        if (!hasFoundEnd)
            return null;

        ActionUnit path = new ActionUnit();
        while (current.parent != null) {
            path.add(current.action);
            current = current.parent;
        }
        Collections.reverse(path.actions);
        return path;
    }

    List<JumpPathNode> getNeighbours(JumpPathNode parent, Vec2i end) {
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

            SimMario newSimMario = parent.simMario.clone();
            newSimMario.move(action);

            // Heuristic is in cell distance, score is in frames? Problem?
            float heuristic = Vec2f.subtract(WorldSpace.cellToFloat(end), newSimMario.body.position).magnitude();
            float score = 1 + parent.scoreTo;

            JumpPathNode n = new JumpPathNode(newSimMario, parent, action, score, heuristic);
            neighbours.add(n);
        }

        return neighbours;
    }
}
