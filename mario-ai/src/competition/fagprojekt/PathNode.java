package competition.fagprojekt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PathNode implements Comparable<PathNode> {
    public Vec2i position;
    public PathNode parent;

    public int scoreTo;
    public Vec2f marioVelocity;
    public List<boolean[]> actions;

    public PathNode(Vec2i position) {
        this.position = position;
        this.parent = null;
        this.scoreTo = 0;
        this.marioVelocity = new Vec2f(0f, 0f);
        this.actions = new ArrayList<>();
    }

    public PathNode(Vec2i position, PathNode parent, int scoreTo, Vec2f marioVelocity) {
        this.position = position;
        this.parent = parent;
        this.scoreTo = scoreTo;
        this.marioVelocity = marioVelocity;
        this.actions = new ArrayList<>();
    }

    @Override
    public int compareTo(PathNode o) {
        return scoreTo > o.scoreTo ? 1 : -1;
    }
}
