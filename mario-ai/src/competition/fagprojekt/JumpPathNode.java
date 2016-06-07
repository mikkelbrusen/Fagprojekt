package competition.fagprojekt;

import ch.idsia.benchmark.mario.environments.Environment;

/**
 * Created by max on 06/06/16.
 */
public class JumpPathNode implements Comparable<JumpPathNode>
{
    public SimMario simMario;
    public JumpPathNode parent;

    public boolean stoppedJumping;
    public float heuristic;
    public float scoreTo;
    public boolean[] action;

    public JumpPathNode(SimMario simMario) {
        this.parent = null;
        this.scoreTo = 0;
        this.heuristic = 0;
        this.simMario = simMario;
        this.stoppedJumping = false;
        this.action = new boolean[Environment.numberOfKeys];
    }

    public JumpPathNode(SimMario simMario, JumpPathNode parent, boolean[] action, float scoreTo, float heuristic) {
        this.parent = parent;
        this.scoreTo = scoreTo;
        this.heuristic = heuristic;
        this.simMario = simMario;
        this.stoppedJumping = parent.stoppedJumping || !action[Environment.MARIO_KEY_JUMP];
        this.action = new boolean[Environment.numberOfKeys];
        System.arraycopy(action, 0, this.action, 0, action.length);
    }

    public Vec2i getCellPosition() {
        return WorldSpace.floatToCell(simMario.body.position);
    }

    @Override
    public int compareTo(JumpPathNode o) {
        return (scoreTo + heuristic) > (o.scoreTo + o.heuristic) ? 1 : -1;
    }
}
