package competition.fagprojekt;

import java.util.ArrayList;
import java.util.List;

public class PathNode implements Comparable<PathNode> {
    public Vec2i position;
    public PathNode parent;

    public ActionUnit actions;
    public Fitness fitness;

    public PathNode(Vec2i position) {
        this.position = position.clone();
        this.parent = null;
        this.actions = new ActionUnit();
        this.fitness = new Fitness();
    }

    public PathNode(Vec2i position, PathNode parent, float scoreTo, float heuristic, Vec2f endPosition, Vec2f endVelocity) {
        this.position = position.clone();
        this.parent = parent;
        this.actions = new ActionUnit();
        this.fitness = new Fitness(scoreTo, heuristic);
        this.actions.endPosition = endPosition.clone();
        this.actions.endVelocity = endVelocity.clone();
    }

    @Override
    public int compareTo(PathNode o) {
        return (fitness.getFitness() > o.fitness.getFitness() ) ? 1 : -1;
    }
}
