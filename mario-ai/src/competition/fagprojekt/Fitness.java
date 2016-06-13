package competition.fagprojekt;

/**
 * Created by Jeppe on 07-06-2016.
 */
public class Fitness {
    float scoreTo;
    float heuristic;

    public Fitness() {
        this.scoreTo = 0;
        this.heuristic = 0;
    }

    public Fitness(float scoreTo, float heuristic) {
        this.scoreTo = scoreTo;
        this.heuristic = heuristic;
    }

    public float getFitness() {
        return this.scoreTo + this.heuristic;
    }

}
