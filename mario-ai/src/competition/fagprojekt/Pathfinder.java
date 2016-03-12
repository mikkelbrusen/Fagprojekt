package competition.fagprojekt;

import java.util.*;

public class Pathfinder {
    WorldSpace worldSpace;

    public Pathfinder(WorldSpace worldSpace) {
        this.worldSpace = worldSpace;
    }

    public List<Vector2i> searchBfs(Vector2i start, Vector2i end) {
        Queue<PathNode> toBeSearched = new LinkedList<>();
        PathNode current = new PathNode(start.clone(), null);

        boolean hasFoundEnd = false;
        toBeSearched.add(current);
        while (!hasFoundEnd && !toBeSearched.isEmpty()) {
            current = toBeSearched.poll();

            if (current.position.equals(end))
                hasFoundEnd = true;
            else {
                for (Vector2i n : getNeighbours(current.position))
                    toBeSearched.add(new PathNode(n, current));
            }
        }

        if (!hasFoundEnd)
            return null;

        List<Vector2i> path = new ArrayList<>();
        while (current.parent != null) {
            path.add(current.position);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    List<Vector2i> getNeighbours(Vector2i p) {
        return getNeighbours(p.x, p.y);
    }

    List<Vector2i> getNeighbours(int x, int y) {
        List<Vector2i> neighbours = new ArrayList<Vector2i>();

        if (isWalkable(x - 1, y))
            neighbours.add(new Vector2i(x - 1, y));

        if (isWalkable(x + 1, y))
            neighbours.add(new Vector2i(x + 1, y));

        if (isEmpty(x, y + 1)) // Falling
            neighbours.add(new Vector2i(x, y + 1));

        // TODO: Add jumps

        return neighbours;
    }

    boolean isEmpty(int x, int y) {
        return isType(x, y, CellType.Empty);
    }
    boolean isWalkable(int x, int y) {
        return isType(x, y, CellType.Walkable);
    }

    boolean isType(int x, int y, CellType type) {
        return isType(worldSpace.getCell(x, y), type);
    }
    boolean isType(Cell cell, CellType type) {
        return cell != null && cell.type == type;
    }
}
