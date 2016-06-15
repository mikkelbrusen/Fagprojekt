package competition.fagprojekt;

/**
 * Created by Jeppe on 07-06-2016.
 */
public class Fitness {
    private float scoreTo;
    private float heuristic;

    public Fitness(float scoreTo, float heuristic) {
        this.scoreTo = scoreTo;
        this.heuristic = heuristic;
    }

    public float getFitness() {
        return this.scoreTo + this.heuristic;
    }

    public float getScoreTo() {
        return scoreTo;
    }

}
