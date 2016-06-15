package competition.fagprojekt;

public class PathNode implements Comparable<PathNode> {
    public Vec2i position;
    public PathNode parent;

    private ActionUnit actionUnit;
    private Fitness fitness;

    public PathNode(Vec2i position, PathNode parent, float scoreTo, float heuristic, ActionUnit actionUnit) {
        this.position = position.clone();
        this.parent = parent;
        this.actionUnit = actionUnit.clone();
        this.fitness = new Fitness(scoreTo, heuristic);
    }

    @Override
    public int compareTo(PathNode o) {
        return fitness.getFitness() > o.fitness.getFitness() ? 1 : -1;
    }

    public ActionUnit getActionUnit() {
        return actionUnit;
    }
}
