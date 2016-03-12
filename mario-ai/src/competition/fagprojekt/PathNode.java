package competition.fagprojekt;

import java.nio.file.Path;

public class PathNode {
    public Vector2i position;
    public PathNode parent;

    public PathNode(Vector2i position, PathNode parent) {
        this.position = position;
        this.parent = parent;
    }
}
