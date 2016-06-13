package competition.fagprojekt;

import java.util.ArrayList;
import java.util.List;

public class PathNode implements Comparable<PathNode> {
    public Vec2i position;
    public PathNode parent;

    public Body2D endBody;
    public ActionUnit actions;
    public Fitness fitness;

    public PathNode(Vec2i position) {
        this.position = position;
        this.parent = null;
        this.endBody = new Body2D(new Vec2f(0, 0), new Vec2f(0, 0));
        this.actions = new ActionUnit();
        this.fitness = new Fitness();
    }

    public PathNode(Vec2i position, PathNode parent, float scoreTo, float heuristic, Vec2f endPosition, Vec2f endVelocity) {
        this.position = position;
        this.parent = parent;
        this.endBody = new Body2D(endPosition, endVelocity);
        this.actions = new ActionUnit();
        this.fitness = new Fitness(scoreTo, heuristic);
    }

    @Override
    public int compareTo(PathNode o) {
        return (fitness.getFitness() > o.fitness.getFitness() ) ? 1 : -1;
    }
}
