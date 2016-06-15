package competition.fagprojekt;

import ch.idsia.benchmark.mario.environments.Environment;

/**
 * Created by max on 06/06/16.
 */
public class JumpPathNode implements Comparable<JumpPathNode>
{
    private SimMario simMario;
    private JumpPathNode parent;
    private Fitness fitness;
    private boolean[] action;

    public JumpPathNode(SimMario simMario, JumpPathNode parent, boolean[] action, float scoreTo, float heuristic) {
        this.parent = parent;
        this.fitness = new Fitness(scoreTo, heuristic);
        this.simMario = simMario;
        this.action = new boolean[Environment.numberOfKeys];
        System.arraycopy(action, 0, this.action, 0, action.length);
    }

    @Override
    public int compareTo(JumpPathNode o) {
        return this.fitness.getFitness() > o.fitness.getFitness() ? 1 : -1;
    }

    public SimMario getSimMario() {
        return simMario;
    }

    public JumpPathNode getParent() {
        return parent;
    }

    public boolean hasStoppedJumping() {
        if (parent == null)
            return false;
        if (simMario.getJumpTime() == 0)
            return true;
        return parent.hasStoppedJumping() || !action[Environment.MARIO_KEY_JUMP];
    }

    public Fitness getFitness() {
        return fitness;
    }

    public boolean[] getAction() {
        return action;
    }
}
