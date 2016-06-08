package competition.fagprojekt;

/**
 * Created by Jeppe on 07-06-2016.
 */
public class Fitness {
    int scoreTo;
    int heuristic;

    public Fitness(int scoreTo, int heuristic) {
        this.scoreTo = scoreTo;
        this.heuristic = heuristic;
    }

    public int getFitness() {
        return this.scoreTo + this.heuristic;
    }

}
