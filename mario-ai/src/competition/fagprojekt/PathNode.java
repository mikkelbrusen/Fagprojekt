package competition.fagprojekt;

public class PathNode implements Comparable<PathNode> {
    private Vec2i cell;
    private PathNode parent;

    private ActionUnit actionUnit;
    private Fitness fitness;

    public PathNode(Vec2i cell, PathNode parent, float scoreTo, float heuristic, ActionUnit actionUnit) {
        this.cell = cell.clone();
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

    public Vec2i getCell() {
        return cell.clone();
    }

    public PathNode getParent() {
        return parent;
    }
}
