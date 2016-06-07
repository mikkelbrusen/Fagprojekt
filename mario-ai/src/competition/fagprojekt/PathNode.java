package competition.fagprojekt;

import java.util.ArrayList;
import java.util.List;

public class PathNode implements Comparable<PathNode> {
    public Vec2i position;
    public PathNode parent;

    public int scoreTo;
    public Vec2f marioVelocity;
    public ActionUnit actions;
    public Fitness fitness;

    public PathNode(Vec2i position) {
        this.position = position;
        this.parent = null;
        this.scoreTo = 0;
        this.marioVelocity = new Vec2f(0f, 0f);
        this.actions = new ActionUnit();
        this.fitness = new Fitness();
    }

    public PathNode(Vec2i position, PathNode parent, int scoreTo, Vec2f marioVelocity) {
        this.position = position;
        this.parent = parent;
        this.scoreTo = scoreTo;
        this.marioVelocity = marioVelocity;
        this.actions = new ActionUnit();
        this.fitness = new Fitness();
    }

    @Override
    public int compareTo(PathNode o) {
        return (fitness.getFitness() > o.fitness.getFitness() ) ? 1 : -1;
    }
}
