package competition.fagprojekt;

public class PathNode {
    public Vec2i position;
    public PathNode parent;

    public PathNode(Vec2i position, PathNode parent) {
        this.position = position;
        this.parent = parent;
    }
}
