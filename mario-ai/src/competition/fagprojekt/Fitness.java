package competition.fagprojekt;

/**
 * Created by Jeppe on 07-06-2016.
 */
public class Fitness {
    int scoreTo;
    int heuristic;

    public Fitness() {
        this.scoreTo = 0;
        this.heuristic = 0;
    }

    public int getFitness() {
        return this.scoreTo + this.heuristic;
    }

}
