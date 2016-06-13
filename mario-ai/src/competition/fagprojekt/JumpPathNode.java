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
    public Fitness fitness;
    public boolean[] action;

    public JumpPathNode(SimMario simMario) {
        this.parent = null;
        this.fitness = new Fitness();
        this.simMario = simMario;
        this.stoppedJumping = false;
        this.action = new boolean[Environment.numberOfKeys];
    }

    public JumpPathNode(SimMario simMario, JumpPathNode parent, boolean[] action, float scoreTo, float heuristic) {
        this.parent = parent;
        this.fitness = new Fitness(scoreTo, heuristic);
        this.simMario = simMario;
        this.stoppedJumping = parent.stoppedJumping || !action[Environment.MARIO_KEY_JUMP];
        this.action = new boolean[Environment.numberOfKeys];
        System.arraycopy(action, 0, this.action, 0, action.length);

        // Enforce no more than 7 frames of jumps
        if (action[Environment.MARIO_KEY_JUMP] && simMario.jumpTime == 0)
            this.stoppedJumping = true;
    }

    @Override
    public int compareTo(JumpPathNode o) {
        return this.fitness.getFitness() > o.fitness.getFitness() ? 1 : -1;
    }
}
